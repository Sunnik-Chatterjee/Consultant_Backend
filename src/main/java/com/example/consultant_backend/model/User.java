package com.example.consultant_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true,nullable = false)
    private String email;
    private String name;
    private String password;
    private String imageUrl;
    private String fcmToken;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private Integer age;
    private String previousDisease;
}
