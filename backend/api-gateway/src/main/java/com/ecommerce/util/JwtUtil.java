package com.ecommerce.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    // ✅ No expiration field needed in gateway — only user-service generates tokens

    // Validate Token
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract Email
    public String extractEmail(String token) {
        return validateToken(token).getSubject();
    }

    // Check if Valid
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // user-service → util/JwtUtil.java
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);  // ✅ Base64 decode
        return Keys.hmacShaKeyFor(keyBytes);
    }
}