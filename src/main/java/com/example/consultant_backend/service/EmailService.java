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
}
