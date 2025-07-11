package org.inharidge.fairact_contract_be.entity.toxic_clause;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Clause {
    private Long id;
    private String text;
    private List<SegmentPosition> segment_positions;
}

