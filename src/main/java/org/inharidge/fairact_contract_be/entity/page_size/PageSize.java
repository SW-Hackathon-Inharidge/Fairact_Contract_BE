package org.inharidge.fairact_contract_be.entity.page_size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.inharidge.fairact_contract_be.dto.clause.PageSizeDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageSize {
    private Long height;
    private Long width;

    public PageSizeDTO toPageSizeDTO() {
        return PageSizeDTO.builder()
                .height(height)
                .width(width)
                .build();
    }
}
