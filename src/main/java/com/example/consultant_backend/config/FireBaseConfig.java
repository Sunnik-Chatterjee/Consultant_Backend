package com.example.consultant_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class FireBaseConfig {

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    @Value("${firebase.service.account.path:firebase-service-account.json}")
    private String serviceAccountPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {

                InputStream serviceAccount = getServiceAccountStream();

                if (serviceAccount == null) {
                    log.error("❌ Firebase service account file not found at: {}", serviceAccountPath);
                    throw new RuntimeException("Firebase service account file not found!");
                }

                log.info("✅ Found Firebase service account at: {}", serviceAccountPath);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket(storageBucket)
                        .build();

                FirebaseApp.initializeApp(options);

                log.info("✅ Firebase initialized successfully");
                log.info("📦 Storage Bucket: {}", storageBucket);

            } else {
                log.info("ℹ️ Firebase already initialized");
            }
        } catch (Exception e) {
            log.error("❌ Failed to initialize Firebase: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    /**
     * Get Firebase service account input stream
     * Tries multiple locations in order:
     * 1. Absolute file path (for Docker/Render)
     * 2. Relative file path (for local development)
     * 3. Classpath resource (for packaged JAR)
     */
    private InputStream getServiceAccountStream() {
        try {
            // Try 1: Absolute path (Docker/Render: /app/firebase-service-account.json)
            if (Files.exists(Paths.get(serviceAccountPath))) {
                log.info("🔍 Loading from absolute path: {}", serviceAccountPath);
                return new FileInputStream(serviceAccountPath);
            }

            // Try 2: Relative path (Local: ./firebase-service-account.json)
            if (Files.exists(Paths.get(".", serviceAccountPath))) {
                log.info("🔍 Loading from relative path: ./{}", serviceAccountPath);
                return new FileInputStream(Paths.get(".", serviceAccountPath).toFile());
            }

            // Try 3: Classpath resource (JAR: src/main/resources/firebase-service-account.json)
            ClassPathResource classPathResource = new ClassPathResource(serviceAccountPath);
            if (classPathResource.exists()) {
                log.info("🔍 Loading from classpath: {}", serviceAccountPath);
                return classPathResource.getInputStream();
            }

            // Try 4: Classpath with leading slash
            ClassPathResource classPathResourceWithSlash = new ClassPathResource("/" + serviceAccountPath);
            if (classPathResourceWithSlash.exists()) {
                log.info("🔍 Loading from classpath: /{}", serviceAccountPath);
                return classPathResourceWithSlash.getInputStream();
            }

            log.error("❌ Firebase service account not found in any location");
            return null;

        } catch (Exception e) {
            log.error("❌ Error loading Firebase service account: {}", e.getMessage());
            return null;
        }
    }
}
