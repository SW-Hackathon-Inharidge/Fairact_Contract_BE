package org.inharidge.fairact_contract_be.dto.clause;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToxicClauseDTO {

    private Long id;
    private String text;
    private List<SegmentPositionDTO> segment_positions;

    private String reason_type;
    private String reason;
    private String suggestion;

    private Long checked_at;    // nullable
    private Boolean is_checked;
}