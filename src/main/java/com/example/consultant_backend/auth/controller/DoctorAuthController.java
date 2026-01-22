package com.example.consultant_backend.auth.controller;

import com.example.consultant_backend.auth.service.DoctorAuthService;
import com.example.consultant_backend.common.ApiResponse;
import com.example.consultant_backend.dto.auth.AuthRequestDTO;
import com.example.consultant_backend.dto.auth.AuthResponseDTO;
import com.example.consultant_backend.dto.auth.GoogleAuthRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/doctor")
@RequiredArgsConstructor
public class DoctorAuthController {

    private final DoctorAuthService doctorAuthService;

    /**
     * Doctor Login - Step 1: Send OTP
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody AuthRequestDTO request) {
        doctorAuthService.loginDoctor(request);
        return ResponseEntity.ok(
                ApiResponse.success("OTP sent to your email. Please verify to continue.")
        );
    }

    /**
     * Doctor Login - Step 2: Verify OTP
     */
    @PostMapping("/verify-login-otp")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> verifyLoginOtp(@Valid @RequestBody AuthRequestDTO request) {
        AuthResponseDTO response = doctorAuthService.verifyLoginOtp(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response)
        );
    }

    /**
     * Doctor Google Sign-In (No OTP)
     */
    @PostMapping("/google-signin")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> googleSignIn(@Valid @RequestBody GoogleAuthRequestDTO request) {
        AuthResponseDTO response = doctorAuthService.googleSignIn(request);
        return ResponseEntity.ok(
                ApiResponse.success("Google sign-in successful", response)
        );
    }

    /**
     * Doctor Forgot Password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody AuthRequestDTO request) {
        doctorAuthService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(
                ApiResponse.success("Password reset OTP sent to your email")
        );
    }

    /**
     * Doctor Reset Password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody AuthRequestDTO request) {
        doctorAuthService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Password reset successfully")
        );
    }
}
