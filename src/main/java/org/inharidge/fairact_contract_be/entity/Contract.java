package org.inharidge.fairact_contract_be.entity;

import lombok.*;
import org.inharidge.fairact_contract_be.dto.ContractDetailDTO;
import org.inharidge.fairact_contract_be.dto.ContractSummaryDTO;
import org.inharidge.fairact_contract_be.entity.page_size.PageSize;
import org.inharidge.fairact_contract_be.entity.toxic_clause.ToxicClause;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "contract")
public class Contract extends BaseUnixTimeEntity {

    @Id
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

    private List<PageSize> page_sizes;
    private List<ToxicClause> clauses;

    public ContractSummaryDTO toContractSummaryDTO() {
        return ContractSummaryDTO.builder()
                .id(id)
                .title(title)
                .owner_id(owner_id)
                .owner_name(owner_name)
                .worker_id(worker_id)
                .worker_email(worker_email)
                .worker_name(worker_name)
                .is_worker_signed(is_worker_signed)
                .is_owner_signed(is_owner_signed)
                .is_invite_accepted(is_invite_accepted)
                .created_at(getCreatedAt())
                .modified_at(getModifiedAt())
                .build();
    }

    public ContractDetailDTO toContractDetailDTO() {
        return ContractDetailDTO.builder()
                .id(id)
                .title(title)
                .owner_id(owner_id)
                .owner_name(owner_name)
                .worker_id(worker_id)
                .worker_email(worker_email)
                .worker_name(worker_name)
                .is_worker_signed(is_worker_signed)
                .is_owner_signed(is_owner_signed)
                .is_invite_accepted(is_invite_accepted)
                .file_uri(file_uri)
                .file_hash(file_hash)
                .file_processed(file_processed)
                .worker_sign_x(worker_sign_x)
                .worker_sign_y(worker_sign_y)
                .worker_sign_scale(worker_sign_scale)
                .worker_sign_page(worker_sign_page)
                .worker_sign_url(worker_sign_url)
                .owner_sign_x(owner_sign_x)
                .owner_sign_y(owner_sign_y)
                .owner_sign_scale(owner_sign_scale)
                .owner_sign_page(owner_sign_page)
                .owner_sign_url(owner_sign_url)
                .page_sizes(page_sizes)
                .clauses(clauses)
                .created_at(getCreatedAt())
                .modified_at(getModifiedAt())
                .build();
    }
}