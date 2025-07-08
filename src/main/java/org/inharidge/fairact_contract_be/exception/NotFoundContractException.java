package org.inharidge.fairact_contract_be.exception;

public class NotFoundContractException extends RuntimeException {
    public NotFoundContractException(String message) {
        super(message);
    }
}
