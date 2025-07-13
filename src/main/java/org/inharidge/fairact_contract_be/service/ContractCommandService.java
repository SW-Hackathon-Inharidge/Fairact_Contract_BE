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
                    .ownerId(userId)
                    .ownerName(userName)
                    .workerId(null)
                    .workerEmail(null)
                    .workerName(null)
                    .isOwnerSigned(false)
                    .isWorkerSigned(false)
                    .isInviteAccepted(false)
                    .fileUri(fileUri)
                    .fileHash(fileHash)
                    .fileProcessed(false)
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
            if (!contract.getOwnerId().equals(userId))
                throw new UnAuthorizedContractAccessException("userId : " + userId);
            minioService.deleteFile(contract.getFileUri());
            String newFileUri = minioService.uploadFile(newContractFile);
            String fileHash = FileHashUtil.getSha1FromMultipartFile(newContractFile);

            contract.setFileUri(newFileUri);
            contract.setFileHash(fileHash);
            contract.setFileProcessed(false);
            contract.setIsOwnerSigned(false);
            contract.setIsWorkerSigned(false);

            contract.setOwnerSignX(null);
            contract.setOwnerSignY(null);
            contract.setOwnerSignScale(null);
            contract.setOwnerSignPage(null);
            contract.setOwnerSignUrl(null);

            contract.setWorkerSignX(null);
            contract.setWorkerSignY(null);
            contract.setWorkerSignScale(null);
            contract.setWorkerSignPage(null);
            contract.setWorkerSignUrl(null);

            contract.setPageSizes(List.of());
            contract.setClauses(List.of());

            Contract saved = contractRepository.save(contract);
            ContractDetailDTO dto = saved.toContractDetailDTO();

            String preSignedUrl = minioService.getPreSignedUrlByBucketUrl(dto.getFile_uri());

            dto.setFile_uri(preSignedUrl);

            // 근로자가 초대된 상태에서만 실시간으로 알림 전송
            if (contract.getOwnerId() != null && contract.getOwnerId().equals(userId) && contract.getIsInviteAccepted())
                sseEmitterManager.sendToUser(saved.getWorkerId(), "contract-detail", dto);

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

        if (contract.getOwnerId() != null && contract.getOwnerId().equals(userId)) {
            contract.setIsOwnerSigned(true);
            contract.setOwnerSignX(contractDigitalSignRequestDTO.getSign_x());
            contract.setOwnerSignY(contractDigitalSignRequestDTO.getSign_y());
            contract.setOwnerSignPage(contractDigitalSignRequestDTO.getSign_page());
            contract.setOwnerSignUrl(signUrl);
        } else if (contract.getWorkerId() != null && contract.getWorkerId().equals(userId)) {
            contract.setIsWorkerSigned(true);
            contract.setWorkerSignX(contractDigitalSignRequestDTO.getSign_x());
            contract.setWorkerSignY(contractDigitalSignRequestDTO.getSign_y());
            contract.setWorkerSignPage(contractDigitalSignRequestDTO.getSign_page());
            contract.setWorkerSignUrl(signUrl);
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
        if (contract.getOwnerId() != null && contract.getOwnerId().equals(userId) && contract.getIsInviteAccepted())
            sseEmitterManager.sendToUser(saved.getWorkerId(), "contract-detail", dto);
        // 근로자에게 서명했을 때, 고용주에게 서명함을 실시간 알림 전송
        if (contract.getWorkerId() != null && contract.getWorkerId().equals(userId))
            sseEmitterManager.sendToUser(saved.getOwnerId(), "contract-detail", dto);

        return dto;
    }

    public void createEmailInfoInRedis(String contractId, Long userId, String opponent_email) {
        if (!contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundContractException("contractId : " + contractId))
                .getOwnerId().equals(userId))
            throw new UnAuthorizedContractAccessException("userId : " + userId);

        if (redisService.exists("email:" + contractId + ":" + opponent_email))
            throw new AlreadySendEmailException("Already Email Sented, opponent_email : " + opponent_email);

        redisService.save("email:" + contractId + ":" + opponent_email, contractId, 60 * 60 * 24);
    }

    public ContractDetailDTO updateContractStateByInviteAccepted(String contractId, Long userId, String userName) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundContractException("contractId : " + contractId));

        contract.setWorkerId(userId);
        contract.setWorkerName(userName);
        contract.setIsInviteAccepted(true);

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
        if (contract.getWorkerId() != null && contract.getWorkerId().equals(userId))
            sseEmitterManager.sendToUser(saved.getOwnerId(), "contract-detail", dto);

        return dto;
    }

    public void updateContractEmailByContractId(String contractId, String workerEmail) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundContractException("contractId : " + contractId));

        contract.setWorkerEmail(workerEmail);
        contractRepository.save(contract);
    }
}
