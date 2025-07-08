package org.inharidge.fairact_contract_be.service;

import org.inharidge.fairact_contract_be.dto.ContractDetailDTO;
import org.inharidge.fairact_contract_be.entity.Contract;
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

    public ContractCommandService(ContractRepository contractRepository, MinioService minioService) {
        this.contractRepository = contractRepository;
        this.minioService = minioService;
    }

    public ContractDetailDTO createContract(MultipartFile contractFile, Long userId) {
        try {
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

    public ContractDetailDTO updateContractFile(MultipartFile newContractFile, Long contractId, Long userId) {
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

            return contractRepository.save(contract).toContractDetailDTO();

        } catch (IOException e) {
            throw new InvalidFileHashException("Invalid file hash operation");
        }
    }
}
