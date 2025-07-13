package org.inharidge.fairact_contract_be.dto.clause;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentPositionDTO {
    private int page_index;
    private List<VertexDTO> vertices;
}