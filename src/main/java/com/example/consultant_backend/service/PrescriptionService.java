package com.example.consultant_backend.service;

import com.example.consultant_backend.model.Appointment;
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

    // Upload prescription image only
    @Transactional
    public Appointment uploadPrescription(Long appointmentId,
                                          MultipartFile prescriptionImage) throws IOException {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Upload to Firebase Storage and get URL
        String prescriptionUrl = storageService.uploadPrescriptionImage(
                prescriptionImage,
                appointmentId,
                "prescription"
        );

        // Save URL to database
        appointment.setPrescriptionImageUrl(prescriptionUrl);
        Appointment updated = appointmentRepo.save(appointment);

        log.info("Prescription uploaded for appointment {}", appointmentId);
        return updated;
    }

    // Upload medicine image only
    @Transactional
    public Appointment uploadMedicine(Long appointmentId,
                                      MultipartFile medicineImage) throws IOException {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        String medicineUrl = storageService.uploadPrescriptionImage(
                medicineImage,
                appointmentId,
                "medicine"
        );
        appointment.setMedicineImageUrl(medicineUrl);
        Appointment updated = appointmentRepo.save(appointment);

        log.info("Medicine image uploaded for appointment {}", appointmentId);
        return updated;
    }

    // Upload both prescription and medicine images together
    @Transactional
    public Appointment uploadBothPrescriptions(Long appointmentId,
                                               MultipartFile prescriptionImage,
                                               MultipartFile medicineImage) throws IOException {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (prescriptionImage != null && !prescriptionImage.isEmpty()) {
            String prescriptionUrl = storageService.uploadPrescriptionImage(
                    prescriptionImage,
                    appointmentId,
                    "prescription"
            );
            appointment.setPrescriptionImageUrl(prescriptionUrl);
        }

        if (medicineImage != null && !medicineImage.isEmpty()) {
            String medicineUrl = storageService.uploadPrescriptionImage(
                    medicineImage,
                    appointmentId,
                    "medicine"
            );
            appointment.setMedicineImageUrl(medicineUrl);
        }

        Appointment updated = appointmentRepo.save(appointment);
        log.info("Prescription and medicine images uploaded for appointment {}", appointmentId);

        return updated;
    }

    // Delete prescription images
    @Transactional
    public void deletePrescriptionImages(Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Delete prescription image from Firebase Storage
        if (appointment.getPrescriptionImageUrl() != null) {
            storageService.deleteImage(appointment.getPrescriptionImageUrl());
            appointment.setPrescriptionImageUrl(null);
        }

        // Delete medicine image from Firebase Storage
        if (appointment.getMedicineImageUrl() != null) {
            storageService.deleteImage(appointment.getMedicineImageUrl());
            appointment.setMedicineImageUrl(null);
        }

        appointmentRepo.save(appointment);
        log.info("Deleted prescription images for appointment {}", appointmentId);
    }
}
