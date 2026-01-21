package com.example.consultant_backend.service;

import com.example.consultant_backend.model.Appointment;
import com.example.consultant_backend.model.AppointmentStatus;
import com.example.consultant_backend.model.Doctor;
import com.example.consultant_backend.model.User;
import com.example.consultant_backend.repo.AppointmentRepo;
import com.example.consultant_backend.repo.DoctorRepo;
import com.example.consultant_backend.repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AppointmentRepo appointmentRepo;
    @Autowired
    private DoctorRepo doctorRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private WebSocketNotificationService webSocketNotificationService;
    @Autowired
    private FCMNotificationService fcmNotificationService;


    @Transactional
    public Appointment bookAppointment(Long userId, Long doctorId, LocalDate appointmentDate, LocalTime appointmentTime) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment savedAppointment= appointmentRepo.save(appointment);
        fcmNotificationService.notifyDoctorNewAppointment(doctor,savedAppointment);
        List<Appointment> pendingList=getPendingAppointments(doctorId);
        webSocketNotificationService.updateDoctorPendingList(doctorId,pendingList);
        return savedAppointment;
    }

    @Transactional
    public Appointment approveAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId).orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(AppointmentStatus.APPROVED);
        Appointment savedAppointment = appointmentRepo.save(appointment);

        emailService.sendAppointmentConfirmationEmail(
                savedAppointment.getUser().getEmail(),
                savedAppointment.getUser().getName(),
                savedAppointment.getDoctor().getName(),
                savedAppointment.getAppointmentDate(),
                savedAppointment.getAppointmentTime()
        );
        fcmNotificationService.notifyUserAppointmentApproved(savedAppointment.getUser(),savedAppointment);
        List<Appointment> pendingList=getPendingAppointments(savedAppointment.getDoctor().getId());
        webSocketNotificationService.updateDoctorPendingList(savedAppointment.getDoctor().getId(),pendingList);
        return savedAppointment;
    }

    @Transactional
    public Appointment rejectAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(AppointmentStatus.REJECTED);
        Appointment savedAppointment = appointmentRepo.save(appointment);

        // Send rejection email asynchronously
        emailService.sendAppointmentRejectionEmail(
                savedAppointment.getUser().getEmail(),
                savedAppointment.getUser().getName(),
                savedAppointment.getDoctor().getName(),
                savedAppointment.getAppointmentDate(),
                savedAppointment.getAppointmentTime()
        );
        List<Appointment> pendingList = getPendingAppointments(savedAppointment.getDoctor().getId());
        webSocketNotificationService.updateDoctorPendingList(savedAppointment.getDoctor().getId(), pendingList);

        return savedAppointment;
    }

    public List<Appointment> getDoctorAppointments(Long doctorId) {
        return appointmentRepo.findByDoctorId(doctorId);
    }

    public List<Appointment> getPendingAppointments(Long doctorId) {
        return appointmentRepo.findByDoctorIdAndStatus(doctorId, AppointmentStatus.PENDING);
    }

    public List<Appointment> getUserAppointments(Long userId) {
        return appointmentRepo.findByUserId(userId);
    }

    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepo.findById(appointmentId).orElseThrow(() -> new RuntimeException("Appointment not found"));
    }
}
