package org.inharidge.fairact_contract_be.service;

import org.inharidge.fairact_contract_be.dto.ContractDetailDTO;
import org.inharidge.fairact_contract_be.dto.request.ContractDigitalSignRequestDTO;
import org.inharidge.fairact_contract_be.entity.Contract;
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

@Service
public class ContractCommandService {

    private final ContractRepository contractRepository;
    private final MinioService minioService;
    private final RedisService redisService;

    public ContractCommandService(ContractRepository contractRepository, MinioService minioService, RedisService redisService) {
        this.contractRepository = contractRepository;
        this.minioService = minioService;
        this.redisService = redisService;
    }

    public ContractDetailDTO createContract(MultipartFile contractFile, Long userId) {
        try {
            //TODO:: 독소조항 검사 요청 보내기 - 비동기, 응답 X, 상태 저장
            String fileUri = minioService.uploadFile(contractFile);
            String fileHash = FileHashUtil.getSha1FromMultipartFile(contractFile);
            Contract contract = Contract.builder()
                    .title(FileNameUtil.extractFilename(contractFile))
                    .ownerId(userId)
                    .workerId(null)
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

    public ContractDetailDTO updateContractFileByReUpload(MultipartFile newContractFile, Long contractId, Long userId) {
        try {
            //TODO:: 독소조항 검사 요청 보내기 - 비동기, 응답 X, 상태 저장
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

            return contractRepository.save(contract).toContractDetailDTO();

        } catch (IOException e) {
            throw new InvalidFileHashException("Invalid file hash operation");
        }
    }

    public ContractDetailDTO updateContractStateAndDigitalSign(Long contractId, Long userId, ContractDigitalSignRequestDTO contractDigitalSignRequestDTO) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundContractException("contractid : " + contractId));

        if (contract.getOwnerId().equals(userId)) {
            contract.setIsOwnerSigned(true);
            contract.setOwnerSignX(contractDigitalSignRequestDTO.getSign_x());
            contract.setOwnerSignY(contractDigitalSignRequestDTO.getSign_y());
        } else if (contract.getWorkerId().equals(userId)) {
            contract.setIsWorkerSigned(true);
            contract.setWorkerSignX(contractDigitalSignRequestDTO.getSign_x());
            contract.setWorkerSignY(contractDigitalSignRequestDTO.getSign_y());
        } else
            throw new UnAuthorizedContractAccessException("userId : " + userId);

        return contractRepository.save(contract).toContractDetailDTO();
    }

    public void createEmailInfoInRedis(Long contractId, Long userId, String opponent_email) {
        if (!contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundContractException("contractId : " + contractId))
                .getOwnerId().equals(userId))
            throw new UnAuthorizedContractAccessException("userId : " + userId);

        if (redisService.exists("email:" + contractId + ":" + opponent_email))
            throw new AlreadySendEmailException("Already Email Sented, opponent_email : " + opponent_email);

        redisService.save("email:" + contractId + ":" + opponent_email, contractId.toString(), 60 * 60 * 24);
    }

    public ContractDetailDTO updateContractStateByInviteAccepted(Long contractId, Long userId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundContractException("contractId : " + contractId));

        contract.setWorkerId(userId);
        contract.setIsInviteAccepted(true);

        return contractRepository.save(contract).toContractDetailDTO();
    }
}
