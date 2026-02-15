package com.example.consultant_backend.auth.service;

import com.example.consultant_backend.dto.auth.AuthRequestDTO;
import com.example.consultant_backend.dto.auth.AuthResponseDTO;
import com.example.consultant_backend.dto.auth.GoogleAuthRequestDTO;
import com.example.consultant_backend.mapper.AuthMapper;
import com.example.consultant_backend.model.Doctor;
import com.example.consultant_backend.model.OtpType;
import com.example.consultant_backend.repo.DoctorRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DoctorAuthService {

    private final DoctorRepo doctorRepo;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final GoogleAuthService googleAuthService;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Doctor Login with Email & Password - Step 1: Send OTP
     */
    @Transactional
    public void loginDoctor(AuthRequestDTO request) {
        // Find doctor
        Doctor doctor = doctorRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check if password exists
        if (doctor.getPassword() == null) {
            throw new RuntimeException("No password set. Please use Google Sign-In or contact admin.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), doctor.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // ✅ Send OTP instead of directly logging in
        otpService.sendOtp(doctor.getEmail(), doctor.getName(), OtpType.LOGIN);

        log.info("Login OTP sent to doctor: {}", doctor.getEmail());
    }

    /**
     * Doctor Login with Email & Password - Step 2: Verify OTP
     */
    @Transactional
    public AuthResponseDTO verifyLoginOtp(AuthRequestDTO request) {
        // Verify OTP
        boolean valid = otpService.verifyOtp(
                request.getEmail(),
                request.getOtp(),
                OtpType.LOGIN
        );

        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Find doctor
        Doctor doctor = doctorRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // ✅ Generate token with EMAIL for doctors
        String token = jwtService.generateTokenWithEmail(doctor.getEmail());

        log.info("✅ Doctor logged in after OTP verification: {}", doctor.getEmail());
        return authMapper.toAuthResponse(doctor, token);
    }

    /**
     * Doctor Google Sign-In (No OTP needed)
     */
    @Transactional
    public AuthResponseDTO googleSignIn(GoogleAuthRequestDTO request) {
        // 1. Verify Google token
        GoogleIdToken.Payload payload = googleAuthService.verifyGoogleToken(request.getIdToken());

        // 2. Extract user info
        String email = googleAuthService.extractEmail(payload);
        String googleId = googleAuthService.extractGoogleId(payload);
        String imageUrl = googleAuthService.extractPictureUrl(payload);

        // 3. Check if doctor exists
        Doctor doctor = doctorRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "Doctor account not found. Please contact admin to create your account first."
                ));

        // 4. Link Google account if not already linked
        if (doctor.getGoogleId() == null) {
            doctor.setGoogleId(googleId);
            if (doctor.getImageUrl() == null) {
                doctor.setImageUrl(imageUrl);
            }
            doctorRepo.save(doctor);
            log.info("Google account linked for doctor: {}", email);
        }

        // ✅ Generate JWT token with EMAIL (No OTP needed for Google)
        String token = jwtService.generateTokenWithEmail(doctor.getEmail());

        log.info("✅ Doctor logged in via Google: {}", email);
        return authMapper.toAuthResponse(doctor, token);
    }

    /**
     * Forgot Password - Send OTP
     */
    @Transactional
    public void forgotPassword(String email) {
        Doctor doctor = doctorRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        otpService.sendOtp(doctor.getEmail(), doctor.getName(), OtpType.PASSWORD_RESET);

        log.info("Password reset OTP sent to doctor: {}", email);
    }

    /**
     * Reset Password with OTP
     */
    @Transactional
    public void resetPassword(AuthRequestDTO request) {
        boolean valid = otpService.verifyOtp(
                request.getEmail(),
                request.getOtp(),
                OtpType.PASSWORD_RESET
        );

        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        Doctor doctor = doctorRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setPassword(passwordEncoder.encode(request.getPassword()));
        doctorRepo.save(doctor);

        log.info("Password reset for doctor: {}", doctor.getEmail());
    }
}
