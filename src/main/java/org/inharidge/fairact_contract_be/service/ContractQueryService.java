package org.inharidge.fairact_contract_be.service;

import org.inharidge.fairact_contract_be.dto.ContractDetailDTO;
import org.inharidge.fairact_contract_be.dto.ContractSummaryDTO;
import org.inharidge.fairact_contract_be.entity.Contract;
import org.inharidge.fairact_contract_be.exception.NotFoundContractException;
import org.inharidge.fairact_contract_be.repository.ContractRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractQueryService {

    private final ContractRepository contractRepository;
    private final MinioService minioService;

    public ContractQueryService(ContractRepository contractRepository, MinioService minioService) {
        this.contractRepository = contractRepository;
        this.minioService = minioService;
    }

    public List<ContractSummaryDTO> findRecentViewedContractByContractIdList(List<String> contractIdList) {
        return contractRepository.findAllById(contractIdList)
                .stream().map(Contract::toContractSummaryDTO)
                .toList();
    }


    public List<ContractSummaryDTO> findTop3ContractsRequiringUserSignByUserId(Long userId) {
        return contractRepository.findTop3UnsignedByOwnerOrWorker(userId, userId)
                .stream().map(Contract::toContractSummaryDTO)
                .toList();
    }

    public List<ContractSummaryDTO> findTop3ContractsRequiringOpponentSignByUserId(Long userId) {
        return contractRepository.findTop3HalfSignedByOwnerOrWorker(userId, userId)
                .stream().map(Contract::toContractSummaryDTO)
                .toList();
    }

    public ContractDetailDTO findContractDetailById(String contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundContractException("contractId : " + contractId));

        ContractDetailDTO contractDetailDTO = contract.toContractDetailDTO();

        String preSignedUrl = minioService.getPreSignedUrlByBucketUrl(contractDetailDTO.getFile_uri());

        contractDetailDTO.setFile_uri(preSignedUrl);

        return contractDetailDTO;
    }
}
