package com.example.consultant_backend.auth.controller;

import com.example.consultant_backend.auth.service.AuthService;
import com.example.consultant_backend.common.ApiResponse;
import com.example.consultant_backend.dto.auth.AuthRequestDTO;
import com.example.consultant_backend.dto.auth.AuthResponseDTO;
import com.example.consultant_backend.dto.auth.GoogleAuthRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserAuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody AuthRequestDTO request) {
        authService.registerUser(request);
        return ResponseEntity.ok(
                ApiResponse.success("Registration successful. Please verify OTP sent to your email.")
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> verifyOtp(@Valid @RequestBody AuthRequestDTO request) {
        AuthResponseDTO response = authService.verifyRegistrationOtp(request);
        return ResponseEntity.ok(
                ApiResponse.success("Email verified successfully", response)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody AuthRequestDTO request) {
        AuthResponseDTO response = authService.loginUser(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response)
        );
    }

    @PostMapping("/google-signin")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> googleSignIn(@Valid @RequestBody GoogleAuthRequestDTO request) {
        AuthResponseDTO response = authService.googleSignIn(request);
        return ResponseEntity.ok(
                ApiResponse.success("Google sign-in successful", response)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody AuthRequestDTO request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(
                ApiResponse.success("Password reset OTP sent to your email")
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody AuthRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Password reset successfully")
        );
    }
}
