package com.example.consultant_backend.service;

import com.example.consultant_backend.model.Appointment;
import com.example.consultant_backend.model.Doctor;
import com.example.consultant_backend.model.User;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class FCMNotificationService {
    @Async
    public void notifyDoctorNewAppointment(Doctor doctor, Appointment appointment) {
        String fcmToken = doctor.getFcmToken();
        if (fcmToken == null || fcmToken.isEmpty()) {
            log.error("Doctor {} has no FCM token registered", doctor.getId());
            return;
        }
        try {
            Map<String, String> data = new HashMap<>();
            data.put("type", "NEW_APPOINTMENT");
            data.put("appointmentId", String.valueOf(appointment.getId()));
            data.put("patientName", appointment.getUser().getName());
            data.put("patientEmail", appointment.getUser().getEmail());
            data.put("patientAge", String.valueOf(appointment.getUser().getAge()));
            data.put("patientGender", appointment.getUser().getGender().toString());
            data.put("appointmentDate", appointment.getAppointmentDate().toString());
            data.put("appointmentTime", appointment.getAppointmentTime()
                    .format(DateTimeFormatter.ofPattern("hh:mm a")));
            data.put("status", appointment.getStatus().toString());
            Notification notification = Notification.builder()
                    .setTitle("🔔 New Appointment Request")
                    .setBody(appointment.getUser().getName() + " requested an appointment on "
                            + appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                            + " at " + appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a")))
                    .build();
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setSound("default")
                            .setColor("#4CAF50")
                            .setChannelId("appointment_notifications")
                            .build())
                    .build();

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .putAllData(data)
                    .setAndroidConfig(androidConfig)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM notification to doctor {}: {}", doctor.getId(), response);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM notification to doctor {}: {}", doctor.getId(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending FCM to doctor {}", doctor.getId(), e);
        }
    }

    @Async
    public void notifyUserAppointmentApproved(User user, Appointment appointment) {
        String fcmToken = user.getFcmToken();

        if (fcmToken == null || fcmToken.isEmpty()) {
            log.warn("User {} has no FCM token registered", user.getId());
            return;
        }

        try {
            Map<String, String> data = new HashMap<>();
            data.put("type", "APPOINTMENT_APPROVED");
            data.put("appointmentId", String.valueOf(appointment.getId()));
            data.put("doctorName", appointment.getDoctor().getName());
            data.put("doctorEmail", appointment.getDoctor().getEmail());
            data.put("appointmentDate", appointment.getAppointmentDate().toString());
            data.put("appointmentTime", appointment.getAppointmentTime()
                    .format(DateTimeFormatter.ofPattern("hh:mm a")));
            data.put("status", "APPROVED");

            Notification notification = Notification.builder()
                    .setTitle("✅ Appointment Confirmed")
                    .setBody("Dr. " + appointment.getDoctor().getName()
                            + " confirmed your appointment on "
                            + appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                            + " at " + appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a")))
                    .build();

            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setSound("default")
                            .setColor("#4CAF50")
                            .setChannelId("appointment_notifications")
                            .build())
                    .build();

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .putAllData(data)
                    .setAndroidConfig(androidConfig)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent approval FCM to user {}: {}", user.getId(), response);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send approval FCM to user {}: {}", user.getId(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending FCM to user {}", user.getId(), e);
        }
    }

    // Send notification to user when appointment is rejected
    @Async
    public void notifyUserAppointmentRejected(User user, Appointment appointment) {
        String fcmToken = user.getFcmToken();

        if (fcmToken == null || fcmToken.isEmpty()) {
            log.warn("User {} has no FCM token registered", user.getId());
            return;
        }

        try {
            Map<String, String> data = new HashMap<>();
            data.put("type", "APPOINTMENT_REJECTED");
            data.put("appointmentId", String.valueOf(appointment.getId()));
            data.put("doctorName", appointment.getDoctor().getName());
            data.put("appointmentDate", appointment.getAppointmentDate().toString());
            data.put("appointmentTime", appointment.getAppointmentTime()
                    .format(DateTimeFormatter.ofPattern("hh:mm a")));
            data.put("status", "REJECTED");

            Notification notification = Notification.builder()
                    .setTitle("❌ Appointment Declined")
                    .setBody("Dr. " + appointment.getDoctor().getName()
                            + " declined your appointment request. Please book another slot.")
                    .build();

            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setSound("default")
                            .setColor("#F44336")
                            .setChannelId("appointment_notifications")
                            .build())
                    .build();

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .putAllData(data)
                    .setAndroidConfig(androidConfig)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent rejection FCM to user {}: {}", user.getId(), response);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send rejection FCM to user {}: {}", user.getId(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending FCM to user {}", user.getId(), e);
        }
    }
}