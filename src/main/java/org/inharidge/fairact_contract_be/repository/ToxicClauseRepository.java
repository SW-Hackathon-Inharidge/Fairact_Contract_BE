package org.inharidge.fairact_contract_be.repository;

import org.inharidge.fairact_contract_be.entity.ToxicClause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToxicClauseRepository extends JpaRepository<ToxicClause, Integer> {
    List<ToxicClause> findAllByContractId(Long contractId);
}
