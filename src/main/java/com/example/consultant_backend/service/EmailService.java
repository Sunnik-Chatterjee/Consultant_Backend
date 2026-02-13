package com.example.consultant_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${app.email.from:sunnikchatterjee21@gmail.com}")
    private String fromEmail;

    @Value("${app.email.from-name:Healthcare Consultant}")
    private String fromName;

    // ==================== CORE EMAIL SENDING ====================

    /**
     * Send email using Brevo HTTP API (not SMTP - works on Render free tier)
     */
    @Async
    private void sendEmail(String toEmail, String toName, String subject, String htmlContent) {
        try {
            Map<String, Object> emailData = new HashMap<>();

            // Sender
            Map<String, String> sender = new HashMap<>();
            sender.put("name", fromName);
            sender.put("email", fromEmail);
            emailData.put("sender", sender);

            // Recipient
            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", toEmail);
            if (toName != null && !toName.isEmpty()) {
                recipient.put("name", toName);
            }
            emailData.put("to", List.of(recipient));

            // Content
            emailData.put("subject", subject);
            emailData.put("htmlContent", htmlContent);

            // Convert to JSON
            String json = objectMapper.writeValueAsString(emailData);

            // Build request
            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(BREVO_API_URL)
                    .addHeader("api-key", brevoApiKey)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("accept", "application/json")
                    .post(body)
                    .build();

            // Execute request
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("✅ Email sent successfully to {} via Brevo HTTP API", toEmail);
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    log.error("❌ Failed to send email to {}: HTTP {} - {}",
                            toEmail, response.code(), responseBody);
                }
            }

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    // ==================== OTP EMAILS ====================

    @Async
    public void sendOtpEmail(String email, String otp, String userName) {
        String subject = "🔐 Your OTP Verification Code - Healthcare Consultant";
        String htmlContent = String.format(
                "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>" +
                        "<div style='background-color: white; padding: 30px; border-radius: 10px;'>" +
                        "<h2 style='color: #2563eb;'>Hello %s,</h2>" +
                        "<p style='font-size: 16px; line-height: 1.6;'>Your OTP verification code is:</p>" +
                        "<div style='background-color: #f0f9ff; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0;'>" +
                        "<h1 style='color: #2563eb; font-size: 36px; margin: 0; letter-spacing: 8px;'>%s</h1>" +
                        "</div>" +
                        "<p style='color: #dc2626; font-weight: bold;'>⏱️ This code will expire in 5 minutes.</p>" +
                        "<p style='color: #6b7280;'>🔒 Do not share this code with anyone.</p>" +
                        "<p style='color: #6b7280; font-size: 14px; margin-top: 30px;'>" +
                        "If you didn't request this code, please ignore this email.</p>" +
                        "<hr style='border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;'>" +
                        "<p style='color: #6b7280; font-size: 12px;'>Best regards,<br>Healthcare Consultant Team</p>" +
                        "</div></div></body></html>",
                userName, otp
        );

        sendEmail(email, userName, subject, htmlContent);
    }

    @Async
    public void sendLoginOtpEmail(String email, String userName, String otp) {
        String subject = "🔐 Login Verification Code - Healthcare Consultant";
        String htmlContent = String.format(
                "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>" +
                        "<div style='background-color: white; padding: 30px; border-radius: 10px;'>" +
                        "<h2 style='color: #2563eb;'>Hello %s,</h2>" +
                        "<p style='font-size: 16px; line-height: 1.6;'>Your login verification OTP is:</p>" +
                        "<div style='background-color: #f0f9ff; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0;'>" +
                        "<h1 style='color: #2563eb; font-size: 36px; margin: 0; letter-spacing: 8px;'>%s</h1>" +
                        "</div>" +
                        "<p style='color: #dc2626; font-weight: bold;'>⏱️ This code will expire in 5 minutes.</p>" +
                        "<p style='color: #dc2626;'>⚠️ If you didn't attempt to login, please secure your account immediately.</p>" +
                        "<hr style='border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;'>" +
                        "<p style='color: #6b7280; font-size: 12px;'>Best regards,<br>Healthcare Consultant Team</p>" +
                        "</div></div></body></html>",
                userName, otp
        );

        sendEmail(email, userName, subject, htmlContent);
    }

    @Async
    public void sendPasswordResetEmail(String email, String userName, String otp) {
        String subject = "🔑 Password Reset Code - Healthcare Consultant";
        String htmlContent = String.format(
                "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>" +
                        "<div style='background-color: white; padding: 30px; border-radius: 10px;'>" +
                        "<h2 style='color: #2563eb;'>Hello %s,</h2>" +
                        "<p style='font-size: 16px; line-height: 1.6;'>Your password reset OTP is:</p>" +
                        "<div style='background-color: #f0f9ff; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0;'>" +
                        "<h1 style='color: #2563eb; font-size: 36px; margin: 0; letter-spacing: 8px;'>%s</h1>" +
                        "</div>" +
                        "<p style='color: #dc2626; font-weight: bold;'>⏱️ This code will expire in 5 minutes.</p>" +
                        "<p style='color: #6b7280;'>If you didn't request a password reset, please ignore this email.</p>" +
                        "<hr style='border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;'>" +
                        "<p style='color: #6b7280; font-size: 12px;'>Best regards,<br>Healthcare Consultant Team</p>" +
                        "</div></div></body></html>",
                userName, otp
        );

        sendEmail(email, userName, subject, htmlContent);
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
        String subject = "✅ Appointment Confirmed - Healthcare Consultant";
        String htmlContent = String.format(
                "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>" +
                        "<div style='background-color: white; padding: 30px; border-radius: 10px;'>" +
                        "<h2 style='color: #10b981;'>✅ Appointment Confirmed!</h2>" +
                        "<p style='font-size: 16px;'>Dear %s,</p>" +
                        "<p>Your appointment has been successfully confirmed.</p>" +
                        "<div style='background-color: #f0fdf4; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                        "<h3 style='color: #059669; margin-top: 0;'>Appointment Details</h3>" +
                        "<table style='width: 100%%; border-collapse: collapse;'>" +
                        "<tr><td style='padding: 8px 0; color: #6b7280;'>Doctor:</td><td style='padding: 8px 0; font-weight: bold;'>Dr. %s</td></tr>" +
                        "<tr><td style='padding: 8px 0; color: #6b7280;'>Date:</td><td style='padding: 8px 0; font-weight: bold;'>%s</td></tr>" +
                        "<tr><td style='padding: 8px 0; color: #6b7280;'>Time:</td><td style='padding: 8px 0; font-weight: bold;'>%s</td></tr>" +
                        "</table>" +
                        "</div>" +
                        "<div style='background-color: #fffbeb; padding: 15px; border-radius: 8px; border-left: 4px solid #f59e0b;'>" +
                        "<p style='margin: 0; color: #92400e;'>📌 Please arrive 10 minutes early</p>" +
                        "<p style='margin: 5px 0 0 0; color: #92400e;'>📋 Bring any relevant medical records</p>" +
                        "</div>" +
                        "<p style='color: #6b7280; margin-top: 20px;'>If you need to reschedule, please contact us as soon as possible.</p>" +
                        "<hr style='border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;'>" +
                        "<p style='color: #6b7280; font-size: 12px;'>Best regards,<br>Healthcare Consultant Team</p>" +
                        "</div></div></body></html>",
                userName, doctorName, formatDate(appointmentDate), formatTime(appointmentTime)
        );

        sendEmail(email, userName, subject, htmlContent);
    }

    @Async
    public void sendAppointmentRejectionEmail(
            String email,
            String userName,
            String doctorName,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    ) {
        String subject = "❌ Appointment Not Available - Healthcare Consultant";
        String htmlContent = String.format(
                "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;'>" +
                        "<div style='background-color: white; padding: 30px; border-radius: 10px;'>" +
                        "<h2 style='color: #dc2626;'>❌ Appointment Not Available</h2>" +
                        "<p style='font-size: 16px;'>Dear %s,</p>" +
                        "<p>We regret to inform you that your appointment request could not be confirmed.</p>" +
                        "<div style='background-color: #fef2f2; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                        "<h3 style='color: #dc2626; margin-top: 0;'>Requested Appointment</h3>" +
                        "<table style='width: 100%%; border-collapse: collapse;'>" +
                        "<tr><td style='padding: 8px 0; color: #6b7280;'>Doctor:</td><td style='padding: 8px 0;'>Dr. %s</td></tr>" +
                        "<tr><td style='padding: 8px 0; color: #6b7280;'>Date:</td><td style='padding: 8px 0;'>%s</td></tr>" +
                        "<tr><td style='padding: 8px 0; color: #6b7280;'>Time:</td><td style='padding: 8px 0;'>%s</td></tr>" +
                        "</table>" +
                        "<p style='color: #dc2626; margin-top: 15px;'><strong>Reason:</strong> The requested time slot is no longer available.</p>" +
                        "</div>" +
                        "<p>Please book another appointment with a different time slot.</p>" +
                        "<p style='color: #6b7280;'>We apologize for any inconvenience.</p>" +
                        "<hr style='border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;'>" +
                        "<p style='color: #6b7280; font-size: 12px;'>Best regards,<br>Healthcare Consultant Team</p>" +
                        "</div></div></body></html>",
                userName, doctorName, formatDate(appointmentDate), formatTime(appointmentTime)
        );

        sendEmail(email, userName, subject, htmlContent);
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
