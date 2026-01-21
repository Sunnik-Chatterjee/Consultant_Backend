package com.example.consultant_backend.service;

import com.example.consultant_backend.model.Appointment;
import com.example.consultant_backend.model.AppointmentStatus;
import com.example.consultant_backend.repo.AppointmentRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final AppointmentRepo appointmentRepo;
    private final FirebaseStorageService storageService;
    private final FCMNotificationService fcmNotificationService;

    @Transactional
    public Appointment uploadImages(Long appointmentId,
                                    MultipartFile prescriptionImage,
                                    MultipartFile medicineImage) {

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Check if appointment is approved
        if (appointment.getStatus() != AppointmentStatus.APPROVED) {
            throw new RuntimeException("Cannot upload images for non-approved appointment");
        }

        boolean prescriptionUploaded = false;

        try {
            if (prescriptionImage != null && !prescriptionImage.isEmpty()) {

                if (appointment.getPrescriptionImageUrl() != null) {
                    storageService.deleteImage(appointment.getPrescriptionImageUrl());
                }
                String prescriptionUrl = storageService.uploadPrescriptionImage(
                        prescriptionImage,
                        appointmentId,
                        "prescription"
                );

                appointment.setPrescriptionImageUrl(prescriptionUrl);
                prescriptionUploaded = true;
                log.info("Prescription uploaded for appointment {}", appointmentId);
            }

            if (medicineImage != null && !medicineImage.isEmpty()) {

                if (appointment.getMedicineImageUrl() != null) {
                    storageService.deleteImage(appointment.getMedicineImageUrl());
                }
                String medicineUrl = storageService.uploadPrescriptionImage(
                        medicineImage,
                        appointmentId,
                        "medicine"
                );

                appointment.setMedicineImageUrl(medicineUrl);
                log.info("Medicine image uploaded for appointment {}", appointmentId);
            }

            Appointment savedAppointment = appointmentRepo.save(appointment);

            if (prescriptionUploaded) {
                fcmNotificationService.notifyUserPrescriptionUploaded(
                        savedAppointment.getUser(),
                        savedAppointment
                );
                log.info("Prescription notification sent to user {}", savedAppointment.getUser().getId());
            }

            return savedAppointment;

        } catch (IOException e) {
            log.error("Failed to upload images for appointment {}: {}", appointmentId, e.getMessage());
            throw new RuntimeException("Failed to upload images: " + e.getMessage());
        }
    }

    @Transactional
    public void deletePrescriptionImages(Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        boolean imagesDeleted = false;

        // Delete prescription image from Firebase Storage
        if (appointment.getPrescriptionImageUrl() != null) {
            boolean deleted = storageService.deleteImage(appointment.getPrescriptionImageUrl());
            if (deleted) {
                appointment.setPrescriptionImageUrl(null);
                imagesDeleted = true;
                log.info("Deleted prescription image for appointment {}", appointmentId);
            }
        }

        // Delete medicine image from Firebase Storage
        if (appointment.getMedicineImageUrl() != null) {
            boolean deleted = storageService.deleteImage(appointment.getMedicineImageUrl());
            if (deleted) {
                appointment.setMedicineImageUrl(null);
                imagesDeleted = true;
                log.info("Deleted medicine image for appointment {}", appointmentId);
            }
        }

        if (!imagesDeleted) {
            throw new RuntimeException("No images to delete for this appointment");
        }

        appointmentRepo.save(appointment);
        log.info("Successfully deleted prescription images for appointment {}", appointmentId);
    }

    public Appointment getPrescriptionByAppointmentId(Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Check if prescription exists
        if (appointment.getPrescriptionImageUrl() == null &&
                appointment.getMedicineImageUrl() == null) {
            throw new RuntimeException("No prescription or medicine images available for this appointment");
        }

        return appointment;
    }
}
