package org.inharidge.fairact_contract_be.entity;

import lombok.*;
import org.inharidge.fairact_contract_be.dto.ContractDetailDTO;
import org.inharidge.fairact_contract_be.dto.ContractSummaryDTO;
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

    private Boolean isOwnerSigned;
    private Boolean isWorkerSigned;
    private Boolean isInviteAccepted;

    private String title;

    private Long ownerId;
    private String ownerName;
    private Long workerId;
    private String workerEmail;
    private String workerName;

    private String fileUri;
    private String fileHash;
    private Boolean fileProcessed;

    private Long workerSignX;
    private Long workerSignY;
    private Long workerSignScale;

    private Long ownerSignX;
    private Long ownerSignY;
    private Long ownerSignScale;

    private List<ToxicClause> clauses;

    public ContractSummaryDTO toContractSummaryDTO() {
        return ContractSummaryDTO.builder()
                .id(id)
                .title(title)
                .owner_id(ownerId)
                .owner_name(ownerName)
                .worker_id(workerId)
                .worker_email(workerEmail)
                .worker_name(workerName)
                .is_worker_signed(isWorkerSigned)
                .is_owner_signed(isOwnerSigned)
                .is_invite_accepted(isInviteAccepted)
                .created_at(getCreatedAt())
                .modified_at(getModifiedAt())
                .build();
    }

    public ContractDetailDTO toContractDetailDTO() {
        return ContractDetailDTO.builder()
                .id(id)
                .title(title)
                .owner_id(ownerId)
                .owner_name(ownerName)
                .worker_id(workerId)
                .worker_email(workerEmail)
                .worker_name(workerName)
                .is_worker_signed(isWorkerSigned)
                .is_owner_signed(isOwnerSigned)
                .is_invite_accepted(isInviteAccepted)
                .file_uri(fileUri)
                .file_hash(fileHash)
                .file_processed(fileProcessed)
                .worker_sign_x(workerSignX)
                .worker_sign_y(workerSignY)
                .worker_sign_scale(workerSignScale)
                .owner_sign_x(ownerSignX)
                .owner_sign_y(ownerSignY)
                .owner_sign_scale(ownerSignScale)
                .clauses(clauses)
                .created_at(getCreatedAt())
                .modified_at(getModifiedAt())
                .build();
    }
}