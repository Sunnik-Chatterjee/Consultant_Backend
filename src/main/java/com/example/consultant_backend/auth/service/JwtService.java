package com.example.consultant_backend.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours (default)
    private Long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * ✅ Generate token with userId for regular users
     */
    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ Generate token with email for doctors
     */
    public String generateTokenWithEmail(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ Extract userId from token (for regular users)
     */
    public Long extractUserId(String token) {
        try {
            String subject = extractClaim(token, Claims::getSubject);
            // Try to parse as Long, if it fails, it's probably an email
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * ✅ Extract email from token (for doctors)
     */
    public String extractEmail(String token) {
        try {
            // First try to get from custom claim
            String email = extractClaim(token, claims -> claims.get("email", String.class));
            if (email != null) {
                return email;
            }

            // If not found, check if subject is an email
            String subject = extractClaim(token, Claims::getSubject);
            if (subject != null && subject.contains("@")) {
                return subject;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ✅ Generic claim extraction
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * ✅ Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * ✅ Validate token
     */
    public boolean validateToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ✅ Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * ✅ Extract expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
