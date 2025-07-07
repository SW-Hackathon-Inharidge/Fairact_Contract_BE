package org.inharidge.fairact_contract_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Getter
@Service
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContractSummaryDTO {
    private Long id;
    private String title;
    private Long owner_id;
    private Long worker_id;
    private Boolean is_owner_signed;
    private Boolean is_worker_signed;
    private Boolean is_invite_accepted;
}
