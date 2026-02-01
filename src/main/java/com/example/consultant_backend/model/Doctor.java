package com.example.consultant_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;   // Set by admin
    private String googleId;   // NULL if only email/password

    private String phoneNumber;
    private String imageUrl;
    private String specialization;  // e.g., "Cardiologist", "Dermatologist"
    private String fcmToken;
}
