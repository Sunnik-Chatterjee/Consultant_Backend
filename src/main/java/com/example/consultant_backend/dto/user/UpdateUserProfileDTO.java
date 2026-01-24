package com.example.consultant_backend.dto.user;

import lombok.Data;

@Data
public class UpdateUserProfileDTO {
    private String name;
    private Integer age;
    private String gender;
    private String phoneNumber;
    private String previousDisease;
    private String imageUrl;
}
