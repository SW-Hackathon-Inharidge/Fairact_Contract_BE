package org.inharidge.fairact_contract_be.exception;

public class NotFoundEmailException extends RuntimeException {
    public NotFoundEmailException(String message) {
        super(message);
    }
}
