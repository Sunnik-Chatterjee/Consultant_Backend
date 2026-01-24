package com.example.consultant_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // ==================== OTP EMAILS ====================

    @Async
    public void sendOtpEmail(String email, String otp, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("sunnikchatterjee21@gmail.com");
            message.setTo(email);
            message.setSubject("🔐 Your OTP Verification Code - Healthcare Consultant");
            message.setText(String.format(
                    "Hello %s,\n\n" +
                            "Your OTP verification code is: %s\n\n" +
                            "This code will expire in 5 minutes.\n" +
                            "Do not share this code with anyone.\n\n" +
                            "If you didn't request this code, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "Healthcare Consultant Team",
                    userName, otp
            ));

            mailSender.send(message);
            log.info("✅ OTP email sent successfully to {}", email);

        } catch (Exception e) {
            log.error("❌ Failed to send OTP email to {}: {}", email, e.getMessage());
        }
    }

    @Async
    public void sendLoginOtpEmail(String email, String userName, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("sunnikchatterjee21@gmail.com");
            message.setTo(email);
            message.setSubject("🔐 Login Verification Code - Healthcare Consultant");
            message.setText(String.format(
                    "Hello %s,\n\n" +
                            "Your login verification OTP is: %s\n\n" +
                            "This code will expire in 5 minutes.\n" +
                            "If you didn't attempt to login, please secure your account immediately.\n\n" +
                            "Best regards,\n" +
                            "Healthcare Consultant Team",
                    userName, otp
            ));

            mailSender.send(message);
            log.info("✅ Login OTP email sent successfully to {}", email);

        } catch (Exception e) {
            log.error("❌ Failed to send login OTP email to {}: {}", email, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String email, String userName, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("sunnikchatterjee21@gmail.com");
            message.setTo(email);
            message.setSubject("🔑 Password Reset Code - Healthcare Consultant");
            message.setText(String.format(
                    "Hello %s,\n\n" +
                            "Your password reset OTP is: %s\n\n" +
                            "This code will expire in 5 minutes.\n" +
                            "If you didn't request a password reset, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "Healthcare Consultant Team",
                    userName, otp
            ));

            mailSender.send(message);
            log.info("✅ Password reset email sent successfully to {}", email);

        } catch (Exception e) {
            log.error("❌ Failed to send password reset email to {}: {}", email, e.getMessage());
        }
    }

    // ==================== APPOINTMENT EMAILS ====================

    @Async
    public void sendAppointmentConfirmationEmail(
            String email,
            String userName,
            String doctorName,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    ) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("sunnikchatterjee21@gmail.com");
            message.setTo(email);
            message.setSubject("✅ Appointment Confirmed - Healthcare Consultant");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Your appointment has been CONFIRMED!\n\n" +
                            "Appointment Details:\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "Doctor: Dr. %s\n" +
                            "Date: %s\n" +
                            "Time: %s\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                            "Please arrive 10 minutes early.\n" +
                            "Bring any relevant medical records.\n\n" +
                            "If you need to reschedule, please contact us as soon as possible.\n\n" +
                            "Best regards,\n" +
                            "Healthcare Consultant Team",
                    userName,
                    doctorName,
                    formatDate(appointmentDate),
                    formatTime(appointmentTime)
            ));

            mailSender.send(message);
            log.info("✅ Appointment confirmation email sent to {}", email);

        } catch (Exception e) {
            log.error("❌ Failed to send appointment confirmation email to {}: {}", email, e.getMessage());
        }
    }

    @Async
    public void sendAppointmentRejectionEmail(
            String email,
            String userName,
            String doctorName,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    ) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("sunnikchatterjee21@gmail.com");
            message.setTo(email);
            message.setSubject("❌ Appointment Not Available - Healthcare Consultant");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "We regret to inform you that your appointment request could not be confirmed.\n\n" +
                            "Requested Appointment:\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "Doctor: Dr. %s\n" +
                            "Date: %s\n" +
                            "Time: %s\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                            "Reason: The requested time slot is no longer available.\n\n" +
                            "Please book another appointment with a different time slot.\n" +
                            "We apologize for any inconvenience.\n\n" +
                            "Best regards,\n" +
                            "Healthcare Consultant Team",
                    userName,
                    doctorName,
                    formatDate(appointmentDate),
                    formatTime(appointmentTime)
            ));

            mailSender.send(message);
            log.info("✅ Appointment rejection email sent to {}", email);

        } catch (Exception e) {
            log.error("❌ Failed to send appointment rejection email to {}: {}", email, e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    private String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        return date.format(formatter);
    }

    private String formatTime(LocalTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return time.format(formatter);
    }
}
