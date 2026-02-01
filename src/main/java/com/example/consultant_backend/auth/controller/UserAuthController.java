package com.example.consultant_backend.auth.controller;

import com.example.consultant_backend.auth.service.AuthService;
import com.example.consultant_backend.common.ApiResponse;
import com.example.consultant_backend.dto.auth.AuthRequestDTO;
import com.example.consultant_backend.dto.auth.AuthResponseDTO;
import com.example.consultant_backend.dto.auth.GoogleAuthRequestDTO;
import com.example.consultant_backend.dto.auth.OtpVerificationDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserAuthController {

    private final AuthService authService;

    /**
     * ✅ Signup - Register with email/password (sends OTP)
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signUp(@Valid @RequestBody AuthRequestDTO request) {
        authService.registerUser(request);
        return ResponseEntity.ok(
                ApiResponse.success("OTP sent to your email. Please verify to complete registration.")
        );
    }

    /**
     * ✅ Verify OTP - Complete registration
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> verifyOtp(@Valid @RequestBody OtpVerificationDTO request) {
        AuthResponseDTO response = authService.verifyRegistrationOtp(request);
        return ResponseEntity.ok(
                ApiResponse.success("Registration successful", response)
        );
    }

    /**
     * ✅ Signin - Email/password login
     */
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> signIn(@Valid @RequestBody AuthRequestDTO request) {
        AuthResponseDTO response = authService.loginUser(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response)
        );
    }

    /**
     * ✅ Google Sign-In
     */
    @PostMapping("/google-signin")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> googleSignIn(@Valid @RequestBody GoogleAuthRequestDTO request) {
        AuthResponseDTO response = authService.googleSignIn(request);
        return ResponseEntity.ok(
                ApiResponse.success("Google sign-in successful", response)
        );
    }

    /**
     * Forgot Password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok(
                ApiResponse.success("Password reset OTP sent to your email")
        );
    }

    /**
     * Reset Password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody AuthRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Password reset successfully")
        );
    }
}
