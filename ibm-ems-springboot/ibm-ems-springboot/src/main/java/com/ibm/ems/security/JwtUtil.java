package com.ibm.ems.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey signingKey;
    private final long expirationMs;

    /**
     * Constructor injection — validates secret at application startup.
     * A missing or weak secret causes a clear startup failure rather than
     * silently deploying with an insecure configuration.
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration.ms:3600000}") long expirationMs) {

        if (secret == null || secret.isBlank() || secret.startsWith("CHANGE_THIS")) {
            throw new IllegalStateException(
                "[SECURITY] JWT_SECRET is not configured. " +
                "Set the JWT_SECRET environment variable. " +
                "Generate a key with: openssl rand -hex 32");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                "[SECURITY] JWT secret is too short. " +
                "Minimum 32 bytes (256 bits) required for HMAC-SHA256.");
        }

        this.signingKey  = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        log.info("JwtUtil initialised. Token expiry: {} ms", expirationMs);
    }

    /**
     * Generate a signed JWT containing the username and Spring Security role names.
     *
     * @param username authenticated user's username
     * @param roles    list of role names e.g. ["ROLE_ADMIN", "ROLE_USER"]
     * @return compact signed JWT string
     */
    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    /** Extract the username (subject) from a token. */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /** Extract roles list from token claims. */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }

    /**
     * Validate token signature and expiry.
     * Returns false on failure — never throws — safe to call in a filter.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getClass().getSimpleName());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
