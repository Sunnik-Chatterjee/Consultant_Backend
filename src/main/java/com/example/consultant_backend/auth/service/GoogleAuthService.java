package com.example.consultant_backend.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String clientId;

    /**
     * Verify Google ID token and extract user information
     *
     * @param idToken - Google ID token from frontend (Google One Tap)
     * @return GoogleIdToken.Payload containing user info
     * @throws RuntimeException if token is invalid
     */
    public GoogleIdToken.Payload verifyGoogleToken(String idToken) {
        try {
            // Create Google ID token verifier
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            // Verify the token
            GoogleIdToken token = verifier.verify(idToken);

            if (token != null) {
                GoogleIdToken.Payload payload = token.getPayload();

                // Extract user info
                String email = payload.getEmail();
                String userId = payload.getSubject();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                Boolean emailVerified = payload.getEmailVerified();

                log.info("Google token verified successfully for email: {}", email);
                log.debug("User ID: {}, Name: {}, Email Verified: {}", userId, name, emailVerified);

                return payload;
            } else {
                log.error("Invalid Google ID token");
                throw new RuntimeException("Invalid Google ID token");
            }

        } catch (Exception e) {
            log.error("Google token verification failed: {}", e.getMessage(), e);
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }

    /**
     * Extract email from Google token payload
     */
    public String extractEmail(GoogleIdToken.Payload payload) {
        return payload.getEmail();
    }

    /**
     * Extract Google user ID from payload
     */
    public String extractGoogleId(GoogleIdToken.Payload payload) {
        return payload.getSubject();
    }

    /**
     * Extract name from Google token payload
     */
    public String extractName(GoogleIdToken.Payload payload) {
        return (String) payload.get("name");
    }

    /**
     * Extract profile picture URL from Google token payload
     */
    public String extractPictureUrl(GoogleIdToken.Payload payload) {
        return (String) payload.get("picture");
    }

    /**
     * Check if email is verified by Google
     */
    public Boolean isEmailVerified(GoogleIdToken.Payload payload) {
        return payload.getEmailVerified();
    }
}
