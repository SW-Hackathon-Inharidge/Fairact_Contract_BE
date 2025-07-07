package org.inharidge.fairact_contract_be.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AuthProvider {
    GOOGLE("GOOGLE"),
    KAKAO("KAKAO"),
    EMAIL("EMAIL");

    private String value;

    AuthProvider(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
