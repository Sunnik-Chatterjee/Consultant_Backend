package com.example.consultant_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OtpVerificationDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email required")
    private String email;

    @NotBlank(message = "OTP is required")
    @Size(min = 4, max = 6, message = "OTP must be 4-6 digits")
    private String otp;
}
