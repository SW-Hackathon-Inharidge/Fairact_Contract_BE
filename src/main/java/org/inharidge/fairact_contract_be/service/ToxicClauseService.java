package org.inharidge.fairact_contract_be.service;

import org.inharidge.fairact_contract_be.entity.ToxicClause;
import org.inharidge.fairact_contract_be.repository.ToxicClauseRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ToxicClauseService {
    private final ToxicClauseRepository toxicClauseRepository;

    public ToxicClauseService(ToxicClauseRepository toxicClauseRepository) {
        this.toxicClauseRepository = toxicClauseRepository;
    }

    public void updateToxicClausesCheckState(Long contractId) {
        List<ToxicClause> toxicClauses =
                toxicClauseRepository.findAllByContractId(contractId);

        for (ToxicClause toxicClause : toxicClauses) {
            toxicClause.setIsChecked(true);
            toxicClause.setCheckedAt(Instant.now().getEpochSecond());
        }

        toxicClauseRepository.saveAll(toxicClauses);
    }
}
