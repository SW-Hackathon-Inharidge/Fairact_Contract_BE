package org.inharidge.fairact_contract_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "contract_view_history")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractViewHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;
}