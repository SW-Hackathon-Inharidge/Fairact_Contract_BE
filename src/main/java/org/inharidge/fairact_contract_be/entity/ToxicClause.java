package org.inharidge.fairact_contract_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "toxic_clause")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToxicClause {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private String reason;

    private String evidence;

    @Column(name = "position_x")
    private Integer positionX;

    @Column(name = "position_y")
    private Integer positionY;

    private Long width;

    private Long height;

    @Column(name = "is_checked")
    private Boolean isChecked;

    @Column(name = "checked_At")
    private Long checkedAt;

    @Column(name = "contract_id")
    private Long contractId;
}
