package org.inharidge.fairact_contract_be.service;

import org.inharidge.fairact_contract_be.entity.ContractViewHistory;
import org.inharidge.fairact_contract_be.repository.ContractViewHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractViewHistoryService {
    private final ContractViewHistoryRepository contractViewHistoryRepository;

    public ContractViewHistoryService(ContractViewHistoryRepository contractViewHistoryRepository) {
        this.contractViewHistoryRepository = contractViewHistoryRepository;
    }

    public List<ContractViewHistory> findRecent3ContractViewHistoryByUserId(Long userId) {
        return contractViewHistoryRepository.findTop3ByUserIdOrderByViewedAtDesc(userId);
    }
}
