package org.inharidge.fairact_contract_be.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractEmailSendingRequestDTO {
    private String opponent_email;
    private String subject;
    private String html_content;
}
