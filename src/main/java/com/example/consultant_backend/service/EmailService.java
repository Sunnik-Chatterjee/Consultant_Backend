package com.example.consultant_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendAppointmentConfirmationEmail(String userEmail, String userName, String doctorName, LocalDate date, LocalTime time) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject("Appointment Confirmed");
            message.setText(String.format("Dear %s, \n" + "Your appointment with Dr. %s has been confirmed \n" + "Date: %s \n" + "Time: %s \n" + "Please arrive 10 minutes early.\n"
                            + "Thank you,\n" + "HealthCare Team"
                    , userName, doctorName, date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), time.format(DateTimeFormatter.ofPattern("hh:mm "))
            ));
            mailSender.send(message);
            log.info("Confirmation Email has been sent to {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send confirmation email to {}", userEmail, e);
        }
    }


    @Async
    public void sendAppointmentRejectionEmail(String userEmail, String userName,
                                              String doctorName, LocalDate date,
                                              LocalTime time) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject("Appointment Declined");
            message.setText(String.format(
                    "Dear %s,\n" +
                            "We regret to inform you that your appointment with Dr. %s has been DECLINED.\n\n" +
                            "Requested Date: %s\n" +
                            "Requested Time: %s\n\n" +
                            "Please contact us to schedule another appointment.\n\n" +
                            "Thank you,\n" +
                            "Healthcare Team",
                    userName, doctorName,
                    date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                    time.format(DateTimeFormatter.ofPattern("hh:mm a"))
            ));

            mailSender.send(message);
            log.info("Rejection email sent to {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send rejection email to {}", userEmail, e);
        }
    }

    @Async
    public void sendOtpEmail(String email, String otp, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your OTP Verification Code");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Your OTP verification code is: %s\n\n" +
                            "This code will expire in 5 minutes.\n" +
                            "Do not share this code with anyone.\n\n" +
                            "Thank you,\n" +
                            "Healthcare Team",
                    userName, otp
            ));
            mailSender.send(message);
            log.info("OTP email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}", email, e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String email, String userName, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset Request");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Your password reset OTP is: %s\n\n" +
                            "This code will expire in 5 minutes.\n\n" +
                            "Thank you,\n" +
                            "Healthcare Team",
                    userName, otp
            ));
            mailSender.send(message);
            log.info("Password reset email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", email, e);
        }
    }

    @Async
    public void sendLoginOtpEmail(String email, String userName, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Login Verification Code");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Your login verification OTP is: %s\n\n" +
                            "This code will expire in 5 minutes.\n" +
                            "If you didn't request this, please ignore this email.\n\n" +
                            "Thank you,\n" +
                            "Healthcare Team",
                    userName, otp
            ));
            mailSender.send(message);
            log.info("Login OTP email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send login OTP email to {}", email, e);
        }
    }


}
