package org.inharidge.fairact_contract_be.service;

import org.inharidge.fairact_contract_be.entity.Contract;
import org.inharidge.fairact_contract_be.entity.toxic_clause.ToxicClause;
import org.inharidge.fairact_contract_be.exception.NotFoundContractException;
import org.inharidge.fairact_contract_be.repository.ContractRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ToxicClauseService {
    private final ContractRepository contractRepository;

    public ToxicClauseService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public void updateToxicClausesCheckState(String contractId) {
        Contract contract =
                contractRepository.findById(contractId)
                        .orElseThrow(() -> new NotFoundContractException("contractId : " + contractId));

        if(contract.getClauses() != null) {
            for (ToxicClause toxicClause : contract.getClauses()) {
                toxicClause.setIsChecked(true);
                toxicClause.setCheckedAt(Instant.now().getEpochSecond());
            }

            contractRepository.save(contract);
        }
    }
}
