package org.inharidge.fairact_contract_be.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.digest.DigestUtils;
import org.inharidge.fairact_contract_be.dto.TokenDTO;
import org.inharidge.fairact_contract_be.entity.AuthProvider;
import org.inharidge.fairact_contract_be.exception.JwtAuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

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

//    public TokenDTO generateToken(Long userId, String email, AuthProvider provider) {
//        Date now = new Date();
//        Date expiryDate = new Date(now.getTime() + tokenValidityInSeconds * 3600);
//
//        String accessToken = Jwts.builder()
//                .setSubject(userId.toString())
//                .claim("email", email)
//                .claim("provider", provider.getValue())
//                .setIssuedAt(now)
//                .setExpiration(expiryDate)
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//
//        String refreshToken = generateRefreshToken();
//        saveRefreshToken(userId.toString(), accessToken, refreshToken);
//
//        return new TokenDTO(accessToken, refreshToken);
//    }

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

    public AuthProvider extractProvider(String token) {
        return AuthProvider.valueOf(validateAccessToken(token).getBody().get("provider", String.class));
    }
}
