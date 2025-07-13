package org.inharidge.fairact_contract_be.service;

import org.inharidge.fairact_contract_be.config.SseEmitterManager;
import org.inharidge.fairact_contract_be.dto.ContractDetailDTO;
import org.inharidge.fairact_contract_be.dto.request.ContractDigitalSignRequestDTO;
import org.inharidge.fairact_contract_be.entity.Contract;
import org.inharidge.fairact_contract_be.entity.page_size.PageSize;
import org.inharidge.fairact_contract_be.entity.toxic_clause.ToxicClause;
import org.inharidge.fairact_contract_be.exception.AlreadySendEmailException;
import org.inharidge.fairact_contract_be.exception.InvalidFileHashException;
import org.inharidge.fairact_contract_be.exception.NotFoundContractException;
import org.inharidge.fairact_contract_be.exception.UnAuthorizedContractAccessException;
import org.inharidge.fairact_contract_be.repository.ContractRepository;
import org.inharidge.fairact_contract_be.util.FileHashUtil;
import org.inharidge.fairact_contract_be.util.FileNameUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class ContractCommandService {

    private final ContractRepository contractRepository;
    private final MinioService minioService;
    private final RedisService redisService;
    private final SseEmitterManager sseEmitterManager;
    private final ToxicClauseService toxicClauseService;

    public ContractCommandService(ContractRepository contractRepository, MinioService minioService, RedisService redisService, SseEmitterManager sseEmitterManager, ToxicClauseService toxicClauseService) {
        this.contractRepository = contractRepository;
        this.minioService = minioService;
        this.redisService = redisService;
        this.sseEmitterManager = sseEmitterManager;
        this.toxicClauseService = toxicClauseService;
    }

    public ContractDetailDTO createContract(MultipartFile contractFile, Long userId, String userName) {
        try {
            String fileUri = minioService.uploadFile(contractFile);
            String fileHash = FileHashUtil.getSha1FromMultipartFile(contractFile);
            Contract contract = Contract.builder()
                    .title(FileNameUtil.extractFilename(contractFile))
                    .owner_id(userId)
                    .owner_name(userName)
                    .worker_id(null)
                    .worker_email(null)
                    .worker_name(null)
                    .is_owner_signed(false)
                    .is_worker_signed(false)
                    .is_invite_accepted(false)
                    .file_uri(fileUri)
                    .file_hash(fileHash)
                    .file_processed(false)
                    .build();

            return contractRepository.save(contract).toContractDetailDTO();

        } catch (IOException e) {
            throw new InvalidFileHashException("Invalid file hash operation");
        }
    }

    public ContractDetailDTO updateContractFileByReUpload(MultipartFile newContractFile, String contractId, Long userId) {
        try {
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new NotFoundContractException("contractId : " + contractId));
            if (!contract.getOwner_id().equals(userId))
                throw new UnAuthorizedContractAccessException("userId : " + userId);
            minioService.deleteFile(contract.getFile_uri());
            String newFileUri = minioService.uploadFile(newContractFile);
            String fileHash = FileHashUtil.getSha1FromMultipartFile(newContractFile);

            contract.setFile_uri(newFileUri);
            contract.setFile_hash(fileHash);
            contract.setFile_processed(false);
            contract.setIs_owner_signed(false);
            contract.setIs_worker_signed(false);

            contract.setOwner_sign_x(null);
            contract.setOwner_sign_y(null);
            contract.setOwner_sign_scale(null);
            contract.setOwner_sign_page(null);
            contract.setOwner_sign_url(null);

            contract.setWorker_sign_x(null);
            contract.setWorker_sign_y(null);
            contract.setWorker_sign_scale(null);
            contract.setWorker_sign_page(null);
            contract.setWorker_sign_url(null);

            contract.setPage_sizes(List.of());
            contract.setClauses(List.of());

            Contract saved = contractRepository.save(contract);
            ContractDetailDTO dto = saved.toContractDetailDTO();

            String preSignedUrl = minioService.getPreSignedUrlByBucketUrl(dto.getFile_uri());

            dto.setFile_uri(preSignedUrl);

            // 근로자가 초대된 상태에서만 실시간으로 알림 전송
            if (contract.getOwner_id() != null && contract.getOwner_id().equals(userId) && contract.getIs_invite_accepted())
                sseEmitterManager.sendToUser(saved.getWorker_id(), "contract-detail", dto);

            return dto;

        } catch (IOException e) {
            throw new InvalidFileHashException("Invalid file hash operation");
        }
    }

    public ContractDetailDTO updateContractStateAndDigitalSign(String contractId, Long userId, ContractDigitalSignRequestDTO contractDigitalSignRequestDTO) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundContractException("contractid : " + contractId));

        String signUrl =
                minioService.parsePresignedUrlToStorageUrl(contractDigitalSignRequestDTO.getPre_signed_sign_uri());

        if (contract.getOwner_id() != null && contract.getOwner_id().equals(userId)) {
            contract.setIs_owner_signed(true);
            contract.setOwner_sign_x(contractDigitalSignRequestDTO.getSign_x());
            contract.setOwner_sign_y(contractDigitalSignRequestDTO.getSign_y());
            contract.setOwner_sign_scale(contractDigitalSignRequestDTO.getSign_page());
            contract.setOwner_sign_url(signUrl);
        } else if (contract.getWorker_id() != null && contract.getWorker_id().equals(userId)) {
            contract.setIs_worker_signed(true);
            contract.setWorker_sign_x(contractDigitalSignRequestDTO.getSign_x());
            contract.setWorker_sign_y(contractDigitalSignRequestDTO.getSign_y());
            contract.setWorker_sign_page(contractDigitalSignRequestDTO.getSign_page());
            contract.setWorker_sign_url(signUrl);
            toxicClauseService.updateToxicClausesCheckState(contractId);
        } else
            throw new UnAuthorizedContractAccessException("userId : " + userId);

        Contract saved = contractRepository.save(contract);
        ContractDetailDTO dto = saved.toContractDetailDTO();

        if (dto.getFile_uri() != null) {
            String preSignedUrl = minioService.getPreSignedUrlByBucketUrl(dto.getFile_uri());
            dto.setFile_uri(preSignedUrl);
        }
        if (dto.getWorker_sign_url() != null) {
            String preSignedUrl = minioService.getPreSignedUrlByBucketUrl(dto.getWorker_sign_url());
            dto.setWorker_sign_url(preSignedUrl);
        }
        if (dto.getOwner_sign_url() != null) {
            String preSignedUrl = minioService.getPreSignedUrlByBucketUrl(dto.getOwner_sign_url());
            dto.setOwner_sign_url(preSignedUrl);
        }

        // 근로자가 초대된 상태에서만 실시간으로 알림 전송
        if (contract.getOwner_id() != null && contract.getOwner_id().equals(userId) && contract.getIs_invite_accepted())
            sseEmitterManager.sendToUser(saved.getWorker_id(), "contract-detail", dto);
        // 근로자에게 서명했을 때, 고용주에게 서명함을 실시간 알림 전송
        if (contract.getWorker_id() != null && contract.getWorker_id().equals(userId))
            sseEmitterManager.sendToUser(saved.getOwner_id(), "contract-detail", dto);

        return dto;
    }

    public void createEmailInfoInRedis(String contractId, Long userId, String opponent_email) {
        if (!contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundContractException("contractId : " + contractId))
                .getOwner_id().equals(userId))
            throw new UnAuthorizedContractAccessException("userId : " + userId);

        if (redisService.exists("email:" + contractId + ":" + opponent_email))
            throw new AlreadySendEmailException("Already Email Sented, opponent_email : " + opponent_email);

        redisService.save("email:" + contractId + ":" + opponent_email, contractId, 60 * 60 * 24);
    }

    public ContractDetailDTO updateContractStateByInviteAccepted(String contractId, Long userId, String userName) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundContractException("contractId : " + contractId));

        contract.setWorker_id(userId);
        contract.setWorker_name(userName);
        contract.setIs_invite_accepted(true);

        Contract saved = contractRepository.save(contract);
        ContractDetailDTO dto = saved.toContractDetailDTO();

        if (dto.getFile_uri() != null) {
            String preSignedUrl = minioService.getPreSignedUrlByBucketUrl(dto.getFile_uri());
            dto.setFile_uri(preSignedUrl);
        }
        if (dto.getWorker_sign_url() != null) {
            String preSignedUrl = minioService.getPreSignedUrlByBucketUrl(dto.getWorker_sign_url());
            dto.setWorker_sign_url(preSignedUrl);
        }
        if (dto.getOwner_sign_url() != null) {
            String preSignedUrl = minioService.getPreSignedUrlByBucketUrl(dto.getOwner_sign_url());
            dto.setOwner_sign_url(preSignedUrl);
        }

        // 근로자에게 초대 수락했을 때, 고용주에게 실시간 알림 전송
        if (contract.getWorker_id() != null && contract.getWorker_id().equals(userId))
            sseEmitterManager.sendToUser(saved.getOwner_id(), "contract-detail", dto);

        return dto;
    }

    public void updateContractEmailByContractId(String contractId, String workerEmail) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundContractException("contractId : " + contractId));

        contract.setWorker_email(workerEmail);
        contractRepository.save(contract);
    }
}
