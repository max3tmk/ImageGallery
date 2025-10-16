package com.innowise.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, UUID userId) {
        return buildToken(username, userId, accessTokenExpiration);
    }

    public String generateRefreshToken(String username, UUID userId) {
        return buildToken(username, userId, refreshTokenExpiration);
    }

    private SecretKey getKey() {
        return key;
    }

    private String buildToken(String username, UUID userId, long expirationMillis) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMillis);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId != null ? userId.toString() : null)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getKey())
                .compact();
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        String raw = claims.get("userId", String.class);
        return raw != null ? UUID.fromString(raw) : null;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return (claims != null) && isTokenNotExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token, String username) {
        String extracted = extractUsername(token);
        return extracted != null && extracted.equals(username) && validateToken(token);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenNotExpired(Claims claims) {
        Date exp = claims.getExpiration();
        return exp != null && !exp.before(new Date());
    }
}
