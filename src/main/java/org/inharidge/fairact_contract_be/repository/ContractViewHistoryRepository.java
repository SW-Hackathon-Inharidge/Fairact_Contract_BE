package org.inharidge.fairact_contract_be.repository;

import org.inharidge.fairact_contract_be.entity.ContractViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractViewHistoryRepository extends JpaRepository<ContractViewHistory, Long> {
    public List<ContractViewHistory> findTop3ByUserIdOrderByViewedAtDesc(Long userId);
}