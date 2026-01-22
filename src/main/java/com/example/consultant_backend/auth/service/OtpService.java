package com.example.consultant_backend.auth.service;

import com.example.consultant_backend.model.OtpType;
import com.example.consultant_backend.model.OtpVerification;
import com.example.consultant_backend.repo.OtpRepo;
import com.example.consultant_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepo otpRepo;
    private final EmailService emailService;

    @Value("${otp.expiration:300000}")
    private Long otpExpiration;

    @Value("${otp.length:6}")
    private Integer otpLength;

    private String generateOtp() {
        int max = (int) Math.pow(10, otpLength) - 1;
        int otp = new Random().nextInt(max);
        return String.format("%0" + otpLength + "d", otp);
    }

    @Transactional
    public boolean verifyOtp(String email, String otp, OtpType otpType) {
        var otpRecord = otpRepo.findByEmailAndOtpAndOtpTypeAndVerifiedFalseAndExpiryTimeAfter(
                email, otp, otpType, Instant.now()
        );

        if (otpRecord.isEmpty()) {
            log.warn("Invalid or expired OTP for email: {}", email);
            return false;
        }

        otpRepo.delete(otpRecord.get());
        log.info("OTP verified successfully for {}", email);
        return true;
    }



    @Transactional
    public void sendOtp(String email, String name, OtpType otpType) {
        otpRepo.deleteByEmailAndOtpType(email, otpType);

        String otp = generateOtp();
        Instant expiryTime = Instant.now().plusMillis(otpExpiration);

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setOtp(otp);
        otpVerification.setOtpType(otpType);
        otpVerification.setExpiryTime(expiryTime);
        otpVerification.setVerified(false);
        otpRepo.save(otpVerification);

        // Send different email based on OTP type
        if (otpType == OtpType.REGISTRATION) {
            emailService.sendOtpEmail(email, otp, name);
        } else if (otpType == OtpType.PASSWORD_RESET) {
            emailService.sendPasswordResetEmail(email, name, otp);
        } else if (otpType == OtpType.LOGIN) {
            emailService.sendLoginOtpEmail(email, name, otp);
        }

        log.info("OTP sent to {} for {}", email, otpType);
    }

}
