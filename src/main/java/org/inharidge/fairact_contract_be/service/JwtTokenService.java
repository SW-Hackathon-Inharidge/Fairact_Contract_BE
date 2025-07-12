package org.inharidge.fairact_contract_be.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.inharidge.fairact_contract_be.entity.AuthProvider;
import org.inharidge.fairact_contract_be.exception.JwtAuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class JwtTokenService {

    private final RedisService redisService;
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity-in-seconds}")
    private long tokenValidityInSeconds;

    private Key key;

    public JwtTokenService(RedisService redisService) {
        this.redisService = redisService;
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Jws<Claims> validateAccessToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (JwtException e) {
            throw new JwtAuthenticationException("Invalid or expired JWT token", e);
        }
    }

    public Long extractUserId(String token) {
        return Long.parseLong(validateAccessToken(token).getBody().getSubject());
    }

    public String extractEmail(String token) {
        return validateAccessToken(token).getBody().get("email", String.class);
    }

    public String extractName(String token) {
        return validateAccessToken(token).getBody().get("name", String.class);
    }

    public AuthProvider extractProvider(String token) {
        return AuthProvider.valueOf(validateAccessToken(token).getBody().get("provider", String.class));
    }
}
