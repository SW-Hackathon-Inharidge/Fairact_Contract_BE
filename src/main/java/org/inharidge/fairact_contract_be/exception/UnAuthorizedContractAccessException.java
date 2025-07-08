package org.inharidge.fairact_contract_be.exception;

public class UnAuthorizedContractAccessException extends RuntimeException {
    public UnAuthorizedContractAccessException(String message) {
        super(message);
    }
}

