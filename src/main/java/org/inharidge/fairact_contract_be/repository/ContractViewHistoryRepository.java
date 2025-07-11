package org.inharidge.fairact_contract_be.repository;

import org.inharidge.fairact_contract_be.entity.ContractViewHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractViewHistoryRepository extends MongoRepository<ContractViewHistory, String> {
    List<ContractViewHistory> findTop3ByUserIdOrderByViewedAtDesc(Long userId);
}