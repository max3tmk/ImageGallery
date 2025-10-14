package com.innowise.common.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() throws Exception {
        jwtUtil = new JwtUtil();

        Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, "super-secret-key-for-tests-1234567890");

        Field accessExpField = JwtUtil.class.getDeclaredField("accessTokenExpiration");
        accessExpField.setAccessible(true);
        accessExpField.set(jwtUtil, 60000L); // 1 min

        Field refreshExpField = JwtUtil.class.getDeclaredField("refreshTokenExpiration");
        refreshExpField.setAccessible(true);
        refreshExpField.set(jwtUtil, 120000L); // 2 min
    }

    @Test
    void generateToken_ShouldContainExpectedClaims() {
        UUID userId = UUID.randomUUID();
        String username = "john";

        String token = jwtUtil.generateToken(username, userId);
        assertNotNull(token);

        String extractedUsername = jwtUtil.extractUsername(token);
        UUID extractedId = jwtUtil.extractUserId(token);

        assertEquals(username, extractedUsername);
        assertEquals(userId, extractedId);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void generateRefreshToken_ShouldWorkSimilarly() {
        UUID userId = UUID.randomUUID();
        String username = "jane";

        String refreshToken = jwtUtil.generateRefreshToken(username, userId);

        assertEquals(username, jwtUtil.extractUsername(refreshToken));
        assertEquals(userId, jwtUtil.extractUserId(refreshToken));
        assertTrue(jwtUtil.validateToken(refreshToken));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenExpired() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor("super-secret-key-for-tests-1234567890".getBytes(StandardCharsets.UTF_8));

        String expired = Jwts.builder()
                .subject("user")
                .issuedAt(Date.from(Instant.now().minusSeconds(3600)))
                .expiration(Date.from(Instant.now().minusSeconds(10)))
                .signWith(key)
                .compact();

        assertFalse(jwtUtil.validateToken(expired));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenInvalid() {
        String invalidToken = "abc.def.ghi";
        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void validateTokenWithUsername_ShouldReturnTrue_WhenUsernameMatches() {
        UUID id = UUID.randomUUID();
        String token = jwtUtil.generateToken("alex", id);

        assertTrue(jwtUtil.validateToken(token, "alex"));
    }

    @Test
    void validateTokenWithUsername_ShouldReturnFalse_WhenUsernameMismatch() {
        UUID id = UUID.randomUUID();
        String token = jwtUtil.generateToken("bob", id);

        assertFalse(jwtUtil.validateToken(token, "alice"));
    }

    @Test
    void extractUserId_ShouldReturnNull_WhenMissing() {
        SecretKey key = Keys.hmacShaKeyFor("super-secret-key-for-tests-1234567890".getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject("no-id-user")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(key)
                .compact();

        assertNull(jwtUtil.extractUserId(token));
    }
}