package com.automobileproject.eap.util;

import com.automobileproject.eap.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiry-ms}")
    private Long expiryMs;

    private SecretKey getSigningKey() {
        String key = secret.length() < 32
                ? String.format("%-32s", secret).replace(' ', '0')
                : secret.substring(0, Math.min(secret.length(), 64));
        return Keys.hmacShaKeyFor(key.getBytes());
    }

    /** Generates a signed JWT with email, role, firstName, lastName as claims. */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiryMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Parses and validates a token, returning all claims. Throws JwtException on failure. */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extracts the email (subject) from a token. */
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    /** Returns true if the token is valid and not expired; catches JwtException internally. */
    public boolean isValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
}
