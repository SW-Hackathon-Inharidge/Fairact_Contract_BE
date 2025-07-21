package org.inharidge.fairact_contract_be.repository;

import org.inharidge.fairact_contract_be.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends MongoRepository<Contract, String> {

    // 🔹 조건: (ownerId == ?0 AND isOwnerSigned == false) OR (workerId == ?1 AND isWorkerSigned == false)
    @Query(value = "{$or: [ " +
            "{ $and: [ { owner_id: ?0 }, { is_owner_signed: false } ] }, " +
            "{ $and: [ { worker_id: ?1 }, { is_worker_signed: false } ] } " +
            "] }", sort = "{ id: -1 }")
    List<Contract> findTop3UnsignedByOwnerOrWorker(Long ownerId, Long workerId);

    // 🔹 조건: (ownerId == ?0 AND isOwnerSigned == true AND isWorkerSigned == false)
    //         OR (workerId == ?1 AND isWorkerSigned == true AND isOwnerSigned == false)
    @Query(value = "{$or: [ " +
            "{ $and: [ { owner_id: ?0 }, { is_owner_signed: true }, { is_worker_signed: false } ] }, " +
            "{ $and: [ { worker_id: ?1 }, { is_worker_signed: true }, { is_owner_signed: false } ] } " +
            "] }", sort = "{ id: -1 }")
    List<Contract> findTop3HalfSignedByOwnerOrWorker(Long ownerId, Long workerId);

    @Query("{ 'owner_id': ?0 }")
    List<Contract> findByOwnerIdCustom(Long ownerId);

    @Query("{ 'owner_id': ?0 }")
    Page<Contract> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("{ 'owner_id': ?0}")
    Page<Contract> findByWorkerId(Long userId, Pageable pageable);
}
