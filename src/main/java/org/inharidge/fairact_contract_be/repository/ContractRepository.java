package org.inharidge.fairact_contract_be.repository;

import org.inharidge.fairact_contract_be.entity.Contract;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends MongoRepository<Contract, String> {

    // 🔹 조건: (ownerId == ?0 AND isOwnerSigned == false) OR (workerId == ?1 AND isWorkerSigned == false)
    @Query(value = "{$or: [ " +
            "{ $and: [ { ownerId: ?0 }, { isOwnerSigned: false } ] }, " +
            "{ $and: [ { workerId: ?1 }, { isWorkerSigned: false } ] } " +
            "] }", sort = "{ id: -1 }")
    List<Contract> findTop3UnsignedByOwnerOrWorker(Long ownerId, Long workerId);

    // 🔹 조건: (ownerId == ?0 AND isOwnerSigned == true AND isWorkerSigned == false)
    //         OR (workerId == ?1 AND isWorkerSigned == true AND isOwnerSigned == false)
    @Query(value = "{$or: [ " +
            "{ $and: [ { ownerId: ?0 }, { isOwnerSigned: true }, { isWorkerSigned: false } ] }, " +
            "{ $and: [ { workerId: ?1 }, { isWorkerSigned: true }, { isOwnerSigned: false } ] } " +
            "] }", sort = "{ id: -1 }")
    List<Contract> findTop3HalfSignedByOwnerOrWorker(Long ownerId, Long workerId);

    List<Contract> findByOwnerId(Long ownerId);
}
