package org.inharidge.fairact_contract_be.util;

import org.inharidge.fairact_contract_be.exception.InvalidAuthorizationHeaderException;

public class AuthorizationHeaderUtil {

    private static final String BEARER_PREFIX = "Bearer ";

    public static String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new InvalidAuthorizationHeaderException("Missing or invalid Authorization header");
        }
        return authHeader.substring(BEARER_PREFIX.length());
    }
}