package org.inharidge.fairact_contract_be.entity.toxic_clause;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clause {
    private Long id;
    private String text;
    private List<SegmentPosition> segment_positions;
}
