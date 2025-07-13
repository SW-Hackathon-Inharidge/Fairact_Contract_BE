package org.inharidge.fairact_contract_be.entity.toxic_clause;

import lombok.*;
import org.inharidge.fairact_contract_be.dto.clause.VertexDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vertex {
    private int x;
    private int y;

    public VertexDTO toVertexDTO() {
        return VertexDTO.builder()
                .x(x)
                .y(y)
                .build();
    }
}
