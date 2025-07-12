package org.inharidge.fairact_contract_be.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractDigitalSignRequestDTO {
    private Long sign_x;
    private Long sign_y;
    private String pre_signed_sign_uri;
}
