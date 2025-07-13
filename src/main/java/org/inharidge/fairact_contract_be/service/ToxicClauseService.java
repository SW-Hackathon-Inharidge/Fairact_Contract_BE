package org.inharidge.fairact_contract_be.service;

import org.inharidge.fairact_contract_be.entity.toxic_clause.ToxicClause;
import org.inharidge.fairact_contract_be.exception.NotFoundContractException;
import org.inharidge.fairact_contract_be.repository.ContractRepository;
import org.inharidge.fairact_contract_be.repository.ToxicClauseRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ToxicClauseService {
    private final ToxicClauseRepository toxicClauseRepository;
    private final ContractRepository contractRepository;

    public ToxicClauseService(ToxicClauseRepository toxicClauseRepository, ContractRepository contractRepository) {
        this.toxicClauseRepository = toxicClauseRepository;
        this.contractRepository = contractRepository;
    }

    public void updateToxicClausesCheckState(String contractId) {
        List<ToxicClause> toxicClauses =
                contractRepository.findById(contractId)
                        .orElseThrow(() -> new NotFoundContractException("contractId : " + contractId))
                        .getClauses();

        if(toxicClauses != null) {
            for (ToxicClause toxicClause : toxicClauses) {
                toxicClause.setIsChecked(true);
                toxicClause.setCheckedAt(Instant.now().getEpochSecond());
            }

            toxicClauseRepository.saveAll(toxicClauses);
        }
    }
}
