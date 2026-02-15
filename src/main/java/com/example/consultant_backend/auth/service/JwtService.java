package com.example.consultant_backend.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours (default)
    private Long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * ✅ Generate token for PATIENT/USER (with userId)
     */
    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "PATIENT");  // ✅ Add user type
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ Generate token for DOCTOR (with doctorId)
     */
    public String generateTokenWithEmail(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "DOCTOR");  // ✅ Add user type
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)  // Store email as subject for now
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ NEW: Generate token for DOCTOR with doctorId
     */
    public String generateDoctorToken(Long doctorId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "DOCTOR");  // ✅ Add user type
        claims.put("email", email);
        claims.put("doctorId", doctorId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(doctorId.toString())  // ✅ Use doctorId as subject
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ Extract user type (DOCTOR or PATIENT)
     */
    public String extractUserType(String token) {
        try {
            return extractClaim(token, claims -> claims.get("type", String.class));
        } catch (Exception e) {
            log.warn("Failed to extract user type from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ✅ Extract subject (userId or doctorId or email)
     */
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * ✅ Extract userId from token (for regular users)
     */
    public Long extractUserId(String token) {
        try {
            String subject = extractSubject(token);
            // Try to parse as Long
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            log.warn("Subject is not a valid userId: {}", e.getMessage());
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
            String subject = extractSubject(token);
            if (subject != null && subject.contains("@")) {
                return subject;
            }

            return null;
        } catch (Exception e) {
            log.warn("Failed to extract email from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ✅ Extract doctorId from token
     */
    public Long extractDoctorId(String token) {
        try {
            // First try from custom claim
            Integer doctorIdInt = extractClaim(token, claims -> claims.get("doctorId", Integer.class));
            if (doctorIdInt != null) {
                return doctorIdInt.longValue();
            }

            // Fallback: try to parse subject as Long
            String subject = extractSubject(token);
            return Long.parseLong(subject);
        } catch (Exception e) {
            log.warn("Failed to extract doctorId from token: {}", e.getMessage());
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
            log.error("Token validation failed: {}", e.getMessage());
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
