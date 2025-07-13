package org.inharidge.fairact_contract_be.entity.toxic_clause;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToxicClause {

    private Clause clause;

    private String reason_type;
    private String reason;
    private String suggestion;

    private Long checked_at;    // nullable
    private Boolean is_checked;
}
