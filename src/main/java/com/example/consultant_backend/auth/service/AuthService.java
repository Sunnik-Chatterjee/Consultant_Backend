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
     * Accepts optional profile data during registration
     * Sends OTP for email verification
     */
    @Transactional
    public void registerUser(AuthRequestDTO request) {
        // Check if user already exists
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Validate password
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);  // Will be verified after OTP

        // ✅ Set profile data if provided
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getAge() != null && request.getAge() > 0) {
            user.setAge(request.getAge());
        }
        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            user.setGender(request.getGender().trim());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getPreviousDisease() != null && !request.getPreviousDisease().trim().isEmpty()) {
            user.setPreviousDisease(request.getPreviousDisease().trim());
        }

        // ✅ Check if profile is complete
        user.setProfileCompleted(isProfileComplete(user));

        // Save user (not verified yet)
        userRepo.save(user);

        // ✅ Send OTP using OtpService.sendOtp()
        String userName = user.getName() != null && !user.getName().isEmpty() ? user.getName() : "User";
        otpService.sendOtp(request.getEmail(), userName, OtpType.REGISTRATION);

        log.info("User registered: {} (Profile complete: {})", request.getEmail(), user.getProfileCompleted());
    }

    /**
     * Verify OTP after registration
     * Returns AuthResponseDTO with profileCompleted flag
     */
    @Transactional
    public AuthResponseDTO verifyRegistrationOtp(OtpVerificationDTO request) {
        // Verify OTP
        boolean valid = otpService.verifyOtp(
                request.getEmail(),
                request.getOtp(),
                OtpType.REGISTRATION
        );

        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Find user
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Mark email as verified
        user.setEmailVerified(true);

        // ✅ Recheck profile completion (in case data changed)
        user.setProfileCompleted(isProfileComplete(user));

        userRepo.save(user);

        // ✅ Generate token with userId
        String token = jwtService.generateToken(user.getId());

        log.info("User email verified: {} (Profile complete: {})", user.getEmail(), user.getProfileCompleted());
        return authMapper.toAuthResponse(user, token);
    }

    /**
     * User Login with Email & Password (Signin)
     * Returns AuthResponseDTO with profileCompleted flag
     */
    public AuthResponseDTO loginUser(AuthRequestDTO request) {
        // Find user
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check if user has password (not Google-only account)
        if (user.getPassword() == null) {
            throw new RuntimeException("No password set. Please use Google Sign-In or reset your password.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Check if email is verified
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email not verified. Please verify your email first.");
        }

        // ✅ Update profile completion status (in case user completed it elsewhere)
        user.setProfileCompleted(isProfileComplete(user));
        userRepo.save(user);

        // ✅ Generate token with userId
        String token = jwtService.generateToken(user.getId());

        log.info("User logged in: {} (Profile complete: {})", user.getEmail(), user.getProfileCompleted());
        return authMapper.toAuthResponse(user, token);
    }

    /**
     * Google Sign-In (Auto create account if not exists)
     * Returns AuthResponseDTO with profileCompleted flag
     */
    @Transactional
    public AuthResponseDTO googleSignIn(GoogleAuthRequestDTO request) {
        // Verify Google token
        GoogleIdToken.Payload payload = googleAuthService.verifyGoogleToken(request.getIdToken());

        // Extract user info from Google
        String email = googleAuthService.extractEmail(payload);
        String googleId = googleAuthService.extractGoogleId(payload);
        String name = googleAuthService.extractName(payload);
        String imageUrl = googleAuthService.extractPictureUrl(payload);
        Boolean emailVerified = googleAuthService.isEmailVerified(payload);

        // Check if user exists
        User user = userRepo.findByEmail(email).orElse(null);

        if (user == null) {
            // ✅ Auto-create new user with Google data
            user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setGoogleId(googleId);
            user.setImageUrl(imageUrl);
            user.setEmailVerified(emailVerified);
            user.setPassword(null);  // No password for Google sign-in

            // ✅ Profile is NOT complete (missing age, gender, phone)
            user.setProfileCompleted(false);

            userRepo.save(user);

            log.info("New user created via Google Sign-In: {} (Profile complete: false)", email);
        } else {
            // Link Google account if not already linked
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
            }

            // Update email verification
            if (!Boolean.TRUE.equals(user.getEmailVerified())) {
                user.setEmailVerified(true);
            }

            // Update image if not set
            if (user.getImageUrl() == null && imageUrl != null) {
                user.setImageUrl(imageUrl);
            }

            // Update name if not set
            if ((user.getName() == null || user.getName().isEmpty()) && name != null) {
                user.setName(name);
            }

            // ✅ Recheck profile completion
            user.setProfileCompleted(isProfileComplete(user));

            userRepo.save(user);

            log.info("Existing user logged in via Google: {} (Profile complete: {})",
                    email, user.getProfileCompleted());
        }

        // ✅ Generate token with userId
        String token = jwtService.generateToken(user.getId());

        return authMapper.toAuthResponse(user, token);
    }

    /**
     * Forgot Password - Send OTP
     */
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if user has password (not Google-only)
        if (user.getPassword() == null) {
            throw new RuntimeException("This account uses Google Sign-In. Please login with Google.");
        }

        // ✅ Send OTP using OtpService.sendOtp()
        String userName = user.getName() != null && !user.getName().isEmpty() ? user.getName() : "User";
        otpService.sendOtp(email, userName, OtpType.PASSWORD_RESET);

        log.info("Password reset OTP sent to: {}", email);
    }

    /**
     * Reset Password with OTP
     */
    @Transactional
    public void resetPassword(AuthRequestDTO request) {
        // Validate password
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        // Validate OTP is provided
        if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
            throw new RuntimeException("OTP is required");
        }

        // Verify OTP
        boolean valid = otpService.verifyOtp(
                request.getEmail(),
                request.getOtp(),
                OtpType.PASSWORD_RESET
        );

        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Find user
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepo.save(user);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    /**
     * Resend OTP for registration
     */
    @Transactional
    public void resendRegistrationOtp(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email already verified");
        }

        // ✅ Send OTP using OtpService.sendOtp()
        String userName = user.getName() != null && !user.getName().isEmpty() ? user.getName() : "User";
        otpService.sendOtp(email, userName, OtpType.REGISTRATION);

        log.info("Registration OTP resent to: {}", email);
    }

    // ==================== HELPER METHODS ====================

    /**
     * ✅ Check if user profile is complete
     * Profile is complete if all required fields are filled
     */
    private boolean isProfileComplete(User user) {
        return user.getName() != null && !user.getName().trim().isEmpty() &&
                user.getAge() != null && user.getAge() > 0 &&
                user.getGender() != null && !user.getGender().trim().isEmpty() &&
                user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty();
    }
}
