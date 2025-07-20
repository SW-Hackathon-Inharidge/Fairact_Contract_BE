package org.inharidge.fairact_contract_be.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractPageListDTO {
    private List<ContractSummaryDTO> contract_summary_list;
    private Long cur_page;
    private Long total_element;
    private Long total_pages;
}
