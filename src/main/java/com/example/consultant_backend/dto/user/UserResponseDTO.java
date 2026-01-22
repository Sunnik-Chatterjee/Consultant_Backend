package com.example.consultant_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private Integer age;
    private String gender;
    private String previousDisease;
    private String phoneNumber;
    private Boolean emailVerified;
    private Instant createdAt;
}
