package com.example.consultant_backend.controller;

import com.example.consultant_backend.common.ApiResponse;
import com.example.consultant_backend.model.Appointment;
import com.example.consultant_backend.model.Doctor;
import com.example.consultant_backend.service.DoctorService;
import com.example.consultant_backend.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class DoctorController {

    private final DoctorService doctorService;
    private final PrescriptionService prescriptionService;

    /**
     * Get all doctors (Public endpoint)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Doctor>>> getAllDoctors() {
        log.info("📋 Get all doctors");

        List<Doctor> doctors = doctorService.getAllDoctors();

        return ResponseEntity.ok(
                ApiResponse.success("Doctors retrieved", doctors)
        );
    }

    /**
     * Get doctor by ID (Public endpoint)
     */
    @GetMapping("/{doctorId}")
    public ResponseEntity<ApiResponse<Doctor>> getDoctorById(@PathVariable Long doctorId) {
        log.info("🔍 Get doctor by ID: {}", doctorId);

        Doctor doctor = doctorService.getDoctorById(doctorId);

        return ResponseEntity.ok(
                ApiResponse.success("Doctor retrieved", doctor)
        );
    }

    /**
     * Get current logged-in doctor's profile
     * Uses JWT token to identify doctor
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Doctor>> getCurrentDoctorProfile(
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();

        log.info("📋 Get profile request from doctor: {}", email);

        Doctor doctor = doctorService.getDoctorByEmail(email);

        return ResponseEntity.ok(
                ApiResponse.success("Doctor profile retrieved", doctor)
        );
    }

    /**
     * Get current doctor's appointments
     */
    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<List<Appointment>>> getCurrentDoctorAppointments(
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();

        log.info("📅 Get appointments for doctor: {}", email);

        Doctor doctor = doctorService.getDoctorByEmail(email);
        List<Appointment> appointments = doctorService.getDoctorAppointments(doctor.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Appointments retrieved", appointments)
        );
    }

    /**
     * Update current doctor's profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Doctor>> updateCurrentDoctorProfile(
            Authentication authentication,
            @RequestBody Doctor updatedDoctor
    ) {
        String email = (String) authentication.getPrincipal();

        log.info("✏️ Update profile for doctor: {}", email);

        Doctor doctor = doctorService.getDoctorByEmail(email);
        Doctor updated = doctorService.updateDoctorProfile(doctor.getId(), updatedDoctor);

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", updated)
        );
    }

    /**
     * Upload prescription & medicine images
     */
    @PostMapping("/appointments/{appointmentId}/upload-images")
    public ResponseEntity<ApiResponse<Appointment>> uploadImages(
            @PathVariable Long appointmentId,
            @RequestParam(value = "prescriptionImage", required = false) MultipartFile prescriptionImage,
            @RequestParam(value = "medicineImage", required = false) MultipartFile medicineImage
    ) {
        log.info("📤 Upload images for appointment: {}", appointmentId);

        if (prescriptionImage == null && medicineImage == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("At least one image (prescription or medicine) must be provided"));
        }

        Appointment appointment = prescriptionService.uploadImages(
                appointmentId,
                prescriptionImage,
                medicineImage
        );

        return ResponseEntity.ok(
                ApiResponse.success("Images uploaded successfully", appointment)
        );
    }
}
