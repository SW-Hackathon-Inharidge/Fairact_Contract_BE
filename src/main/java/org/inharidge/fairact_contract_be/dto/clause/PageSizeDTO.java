package org.inharidge.fairact_contract_be.dto.clause;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageSizeDTO {
    private Long height;
    private Long width;
}