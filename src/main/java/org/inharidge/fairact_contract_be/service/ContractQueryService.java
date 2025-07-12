package org.inharidge.fairact_contract_be.service;

import org.inharidge.fairact_contract_be.dto.ContractDetailDTO;
import org.inharidge.fairact_contract_be.dto.ContractSummaryDTO;
import org.inharidge.fairact_contract_be.entity.Contract;
import org.inharidge.fairact_contract_be.entity.ContractViewHistory;
import org.inharidge.fairact_contract_be.exception.NotFoundContractException;
import org.inharidge.fairact_contract_be.repository.ContractRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractQueryService {

    private final ContractViewHistoryService contractViewHistoryService;
    private final ContractRepository contractRepository;

    public ContractQueryService(ContractViewHistoryService contractViewHistoryService, ContractRepository contractRepository) {
        this.contractViewHistoryService = contractViewHistoryService;
        this.contractRepository = contractRepository;
    }

    public List<ContractSummaryDTO> findRecentViewedContractByUserId(Long userId) {
        List<String> contractViewHistoryIds =
                contractViewHistoryService.findRecent3ContractViewHistoryByUserId(userId)
                        .stream().map(ContractViewHistory::getContractId)
                        .toList();

        return contractRepository.findAllById(contractViewHistoryIds)
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

        return contract.toContractDetailDTO();
    }
}
