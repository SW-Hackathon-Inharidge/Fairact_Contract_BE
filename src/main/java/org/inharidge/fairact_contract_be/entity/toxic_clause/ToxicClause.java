package org.inharidge.fairact_contract_be.entity.toxic_clause;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import lombok.*;
import org.inharidge.fairact_contract_be.dto.clause.ToxicClauseDTO;

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

    public ToxicClauseDTO toToxicClauseDTO() {
        return ToxicClauseDTO.builder()
                .id(id)
                .text(text)
                .segment_positions(segmentPositions.stream().map(SegmentPosition::toSegmentPositionDTO).toList())
                .reason_type(reasonType)
                .reason(reason)
                .suggestion(suggestion)
                .checked_at(checkedAt)
                .is_checked(isChecked)
                .build();
    }
}
