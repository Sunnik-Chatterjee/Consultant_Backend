package com.example.consultant_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;
    private String googleId;

    private Integer age;
    private String gender;
    private String phoneNumber;
    private String previousDisease;
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    private String fcmToken;
    private String imageUrl;

    @Column(name = "profile_completed")
    private Boolean profileCompleted = false;
}
