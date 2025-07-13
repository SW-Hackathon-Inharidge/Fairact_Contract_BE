package org.inharidge.fairact_contract_be.dto;

import lombok.*;
import org.inharidge.fairact_contract_be.dto.clause.PageSizeDTO;
import org.inharidge.fairact_contract_be.dto.clause.ToxicClauseDTO;
import org.inharidge.fairact_contract_be.entity.page_size.PageSize;
import org.inharidge.fairact_contract_be.entity.toxic_clause.ToxicClause;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractDetailDTO {
    private String id;
    private String title;
    private Long owner_id;
    private String owner_name;
    private Long worker_id;
    private String worker_email;
    private String worker_name;
    private Boolean is_owner_signed;
    private Boolean is_worker_signed;
    private Boolean is_invite_accepted;
    private String file_uri;
    private String file_hash;
    private Boolean file_processed;
    private Long worker_sign_x;
    private Long worker_sign_y;
    private Long worker_sign_scale;
    private Long worker_sign_page;
    private String worker_sign_url;
    private Long owner_sign_x;
    private Long owner_sign_y;
    private Long owner_sign_scale;
    private Long owner_sign_page;
    private String owner_sign_url;
    private List<PageSizeDTO> page_sizes;
    private List<ToxicClauseDTO> clauses;
    private Long created_at;
    private Long modified_at;
}
