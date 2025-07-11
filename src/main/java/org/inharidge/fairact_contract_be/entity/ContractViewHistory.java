package org.inharidge.fairact_contract_be.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "contract_view_history")
public class ContractViewHistory {

    @Id
    private String id; // MongoDB 기본 키는 String 타입이 일반적

    private String contractId;
    private Long userId;

    private LocalDateTime viewedAt;
}