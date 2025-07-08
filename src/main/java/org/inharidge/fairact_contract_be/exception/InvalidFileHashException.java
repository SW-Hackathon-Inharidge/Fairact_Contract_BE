package org.inharidge.fairact_contract_be.exception;

public class InvalidFileHashException extends RuntimeException {
    public InvalidFileHashException(String message) {
        super(message);
    }
}
