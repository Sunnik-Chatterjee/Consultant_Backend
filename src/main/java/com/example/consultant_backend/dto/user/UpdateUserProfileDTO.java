package com.example.consultant_backend.dto.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserProfileDTO {
    private String name;

    @Min(value = 1, message = "Age must be positive")
    private Integer age;

    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    private String gender;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phoneNumber;

    private String previousDisease;
}
