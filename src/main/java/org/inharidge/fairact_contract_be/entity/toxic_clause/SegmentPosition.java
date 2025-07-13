package org.inharidge.fairact_contract_be.entity.toxic_clause;

import lombok.*;
import org.inharidge.fairact_contract_be.dto.clause.SegmentPositionDTO;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentPosition {
    private int pageIndex;
    private List<Vertex> vertices;

    public SegmentPositionDTO toSegmentPositionDTO() {
        return SegmentPositionDTO.builder()
                .page_index(pageIndex)
                .vertices(vertices.stream().map(Vertex::toVertexDTO).toList())
                .build();
    }
}