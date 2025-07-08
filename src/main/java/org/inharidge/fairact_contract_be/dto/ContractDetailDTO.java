package org.inharidge.fairact_contract_be.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractDetailDTO {
    private Long id;
    private String title;
    private Long owner_id;
    private Long worker_id;
    private Boolean is_owner_signed;
    private Boolean is_worker_signed;
    private Boolean is_invite_accepted;
    private String file_uri;
    private String file_hash;
    private Boolean file_processed;
    private Long worker_sign_x;
    private Long worker_sign_y;
    private Long owner_sign_x;
    private Long owner_sign_y;
}
