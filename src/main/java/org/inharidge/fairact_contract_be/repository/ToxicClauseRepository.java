package org.inharidge.fairact_contract_be.repository;

import org.inharidge.fairact_contract_be.entity.toxic_clause.ToxicClause;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToxicClauseRepository extends MongoRepository<ToxicClause, String> {
    List<ToxicClause> findAllBy(String clauseId); // 내부 clause.id 기준
}
