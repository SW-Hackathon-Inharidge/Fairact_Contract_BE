package org.inharidge.fairact_contract_be.repository;

import org.inharidge.fairact_contract_be.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    // ownerId가 userId이고 isOwnerSigned가 false이거나,
    // workerId가 userId이고 isWorkerSigned가 false인 계약 Top3 (id 내림차순)
    List<Contract> findTop3ByOwnerIdAndIsOwnerSignedFalseOrWorkerIdAndIsWorkerSignedFalseOrderByIdDesc(Long ownerId, Long workerId);

    List<Contract> findTop3ByOwnerIdAndIsOwnerSignedTrueAndIsWorkerSignedFalseOrWorkerIdAndIsWorkerSignedTrueAndIsOwnerSignedFalseOrderByIdDesc(Long ownerId, Long workerId);
}
