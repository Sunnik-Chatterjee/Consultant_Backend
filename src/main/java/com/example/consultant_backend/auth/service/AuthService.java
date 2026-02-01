package com.example.consultant_backend.auth.service;

import com.example.consultant_backend.dto.auth.AuthRequestDTO;
import com.example.consultant_backend.dto.auth.AuthResponseDTO;
import com.example.consultant_backend.dto.auth.GoogleAuthRequestDTO;
import com.example.consultant_backend.dto.auth.OtpVerificationDTO;
import com.example.consultant_backend.mapper.AuthMapper;
import com.example.consultant_backend.model.OtpType;
import com.example.consultant_backend.model.User;
import com.example.consultant_backend.repo.UserRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final GoogleAuthService googleAuthService;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * User Registration with Email & Password (Signup)
     */
    @Transactional
    public void registerUser(AuthRequestDTO request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = authMapper.toUserEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);
        userRepo.save(user);

        // Send OTP
        otpService.sendOtp(user.getEmail(), user.getName(), OtpType.REGISTRATION);

        log.info("User registered: {}", user.getEmail());
    }

    /**
     * Verify OTP after registration
     */
    @Transactional
    public AuthResponseDTO verifyRegistrationOtp(OtpVerificationDTO request) {
        boolean valid = otpService.verifyOtp(
                request.getEmail(),
                request.getOtp(),
                OtpType.REGISTRATION
        );

        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailVerified(true);
        userRepo.save(user);

        // ✅ Generate token with userId
        String token = jwtService.generateToken(user.getId());

        log.info("User email verified: {}", user.getEmail());
        return authMapper.toAuthResponse(user, token);
    }

    /**
     * User Login with Email & Password (Signin)
     */
    public AuthResponseDTO loginUser(AuthRequestDTO request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (user.getPassword() == null) {
            throw new RuntimeException("No password set. Please use Google Sign-In or reset password.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.getEmailVerified()) {
            throw new RuntimeException("Email not verified. Please verify your email first.");
        }

        // ✅ Generate token with userId
        String token = jwtService.generateToken(user.getId());

        log.info("User logged in: {}", user.getEmail());
        return authMapper.toAuthResponse(user, token);
    }

    /**
     * Google Sign-In (Auto create account if not exists)
     */
    @Transactional
    public AuthResponseDTO googleSignIn(GoogleAuthRequestDTO request) {
        // Verify Google token
        GoogleIdToken.Payload payload = googleAuthService.verifyGoogleToken(request.getIdToken());

        // Extract user info
        String email = googleAuthService.extractEmail(payload);
        String googleId = googleAuthService.extractGoogleId(payload);
        String name = googleAuthService.extractName(payload);
        String imageUrl = googleAuthService.extractPictureUrl(payload);
        Boolean emailVerified = googleAuthService.isEmailVerified(payload);

        // Check if user exists
        User user = userRepo.findByEmail(email).orElse(null);

        if (user == null) {
            // Auto-create new user
            user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setGoogleId(googleId);
            user.setImageUrl(imageUrl);
            user.setEmailVerified(emailVerified);
            user.setPassword(null);  // No password for Google sign-in
            userRepo.save(user);

            log.info("New user created via Google Sign-In: {}", email);
        } else {
            // Link Google account if not already linked
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                user.setEmailVerified(true);
                if (user.getImageUrl() == null) {
                    user.setImageUrl(imageUrl);
                }
                userRepo.save(user);
                log.info("Google account linked for existing user: {}", email);
            }
        }

        // ✅ Generate token with userId
        String token = jwtService.generateToken(user.getId());

        log.info("User logged in via Google: {}", email);
        return authMapper.toAuthResponse(user, token);
    }

    /**
     * Forgot Password - Send OTP
     */
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        otpService.sendOtp(user.getEmail(), user.getName(), OtpType.PASSWORD_RESET);

        log.info("Password reset OTP sent to: {}", email);
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

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepo.save(user);

        log.info("Password reset for user: {}", user.getEmail());
    }
}
