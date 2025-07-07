package org.inharidge.fairact_contract_be.repository;

import org.inharidge.fairact_contract_be.entity.ToxicClause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToxicClauseRepository extends JpaRepository<ToxicClause, Integer> {

}
