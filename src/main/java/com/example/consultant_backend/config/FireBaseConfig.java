package com.example.consultant_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Configuration
@Slf4j
public class FireBaseConfig {

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    @PostConstruct
    public void initialize() {
        try {

            if (FirebaseApp.getApps().isEmpty()) {

                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");

                if (!resource.exists()) {
                    log.error("firebase-service-account.json NOT FOUND");
                    throw new RuntimeException("Firebase service account file not found!");
                }

                log.info("Found firebase-service-account.json");

                InputStream serviceAccount = resource.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket(storageBucket)
                        .build();

                FirebaseApp.initializeApp(options);

                log.info("Storage Bucket: {}", storageBucket);

            } else {
                log.info("Firebase already initialized");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}
