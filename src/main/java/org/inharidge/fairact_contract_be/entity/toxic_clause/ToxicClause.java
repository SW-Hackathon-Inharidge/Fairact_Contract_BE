package org.inharidge.fairact_contract_be.entity.toxic_clause;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToxicClause {

    private Long id;
    private String text;
    private List<SegmentPosition> segmentPositions;

    private String reasonType;
    private String reason;
    private String suggestion;

    private Long checkedAt;    // nullable
    private Boolean isChecked;
}
