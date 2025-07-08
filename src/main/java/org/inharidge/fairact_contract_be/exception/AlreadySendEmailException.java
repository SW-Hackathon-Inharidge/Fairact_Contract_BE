package org.inharidge.fairact_contract_be.exception;

public class AlreadySendEmailException extends RuntimeException {
    public AlreadySendEmailException(String message) {
        super(message);
    }
}
