package com.example.consultant_backend.repo;

import com.example.consultant_backend.model.OtpVerification;
import com.example.consultant_backend.model.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OtpRepo extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findByEmailAndOtpAndOtpTypeAndVerifiedFalseAndExpiryTimeAfter(
            String email,
            String otp,
            OtpType otpType,
            Instant currentTime
    );

    void deleteByEmailAndOtpType(String email, OtpType otpType);

    void deleteByExpiryTimeBefore(Instant currentTime);
}
