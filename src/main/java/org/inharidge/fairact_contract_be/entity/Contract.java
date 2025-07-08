package org.inharidge.fairact_contract_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.inharidge.fairact_contract_be.dto.ContractDetailDTO;
import org.inharidge.fairact_contract_be.dto.ContractSummaryDTO;

@EqualsAndHashCode(callSuper = true)
@Table(name = "contract")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract extends BaseUnixTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "worker_id")
    private Long workerId;

    @Column(name = "is_owner_signed")
    private Boolean isOwnerSigned;

    @Column(name = "is_worker_signed")
    private Boolean isWorkerSigned;

    @Column(name = "is_invite_accepted")
    private Boolean isInviteAccepted;

    @Column(name = "file_uri")
    private String fileUri;

    @Column(name = "file_hash")
    private String fileHash;

    @Column(name = "file_processed")
    private Boolean fileProcessed;

    @Column(name = "worker_sign_x")
    private Long workerSignX;

    @Column(name = "worker_sign_y")
    private Long workerSignY;

    @Column(name = "owner_sign_x")
    private Long ownerSignX;

    @Column(name = "owner_sign_y")
    private Long ownerSignY;

    public ContractSummaryDTO toContractSummaryDTO() {
        return ContractSummaryDTO.builder()
                .id(id)
                .title(title)
                .owner_id(ownerId)
                .worker_id(workerId)
                .is_worker_signed(isWorkerSigned)
                .is_owner_signed(isOwnerSigned)
                .is_invite_accepted(isInviteAccepted)
                .build();
    }

    public ContractDetailDTO toContractDetailDTO() {
        return ContractDetailDTO.builder()
                .id(id)
                .title(title)
                .owner_id(ownerId)
                .worker_id(workerId)
                .is_worker_signed(isWorkerSigned)
                .is_owner_signed(isOwnerSigned)
                .is_invite_accepted(isInviteAccepted)
                .file_uri(fileUri)
                .file_hash(fileHash)
                .file_processed(fileProcessed)
                .worker_sign_x(workerSignX)
                .worker_sign_y(workerSignY)
                .owner_sign_x(ownerSignX)
                .owner_sign_y(ownerSignY)
                .build();
    }
}
