package com.example.consultant_backend.service;

import com.example.consultant_backend.model.Appointment;
import com.example.consultant_backend.model.Doctor;
import com.example.consultant_backend.repo.AppointmentRepo;
import com.example.consultant_backend.repo.DoctorRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepo doctorRepo;
    private final AppointmentRepo appointmentRepo;

    /**
     * Get all doctors
     */
    public List<Doctor> getAllDoctors() {
        log.info("📋 Fetching all doctors");
        return doctorRepo.findAll();
    }

    /**
     * Get doctor by email (used after login)
     */
    public Doctor getDoctorByEmail(String email) {
        log.info("🔍 Fetching doctor by email: {}", email);
        return doctorRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found with email: " + email));
    }

    /**
     * Get doctor by ID
     */
    public Doctor getDoctorById(Long doctorId) {
        log.info("🔍 Fetching doctor by ID: {}", doctorId);
        return doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));
    }

    /**
     * Get doctor's all appointments
     */
    public List<Appointment> getDoctorAppointments(Long doctorId) {
        log.info("📅 Fetching appointments for doctor ID: {}", doctorId);

        // Verify doctor exists
        if (!doctorRepo.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found with ID: " + doctorId);
        }

        return appointmentRepo.findByDoctorId(doctorId);
    }

    /**
     * Update doctor profile
     */
    @Transactional
    public Doctor updateDoctorProfile(Long doctorId, Doctor updatedDoctor) {
        log.info("✏️ Updating profile for doctor ID: {}", doctorId);

        Doctor doctor = getDoctorById(doctorId);

        if (updatedDoctor.getName() != null) {
            doctor.setName(updatedDoctor.getName());
        }
        if (updatedDoctor.getPhoneNumber() != null) {
            doctor.setPhoneNumber(updatedDoctor.getPhoneNumber());
        }
        if (updatedDoctor.getSpecialization() != null) {
            doctor.setSpecialization(updatedDoctor.getSpecialization());
        }
        if (updatedDoctor.getImageUrl() != null) {
            doctor.setImageUrl(updatedDoctor.getImageUrl());
        }

        return doctorRepo.save(doctor);
    }
}
