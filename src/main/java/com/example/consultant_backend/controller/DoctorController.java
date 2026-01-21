package com.example.consultant_backend.controller;


import com.example.consultant_backend.common.ApiResponse;
import com.example.consultant_backend.model.Appointment;
import com.example.consultant_backend.model.Doctor;
import com.example.consultant_backend.repo.DoctorRepo;
import com.example.consultant_backend.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DoctorController {
    @Autowired
    private DoctorRepo doctorRepo;
    @Autowired
    private PrescriptionService prescriptionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Doctor>>> getAllDoctors() {
        return ResponseEntity.ok(
                ApiResponse.success("Doctors retrieved", doctorRepo.findAll())
        );
    }

    @PostMapping("/upload-images/{appointmentId}")
    public ResponseEntity<ApiResponse<Appointment>> uploadImages(
            @PathVariable Long appointmentId,
            @RequestParam(value = "prescriptionImage", required = false) MultipartFile prescriptionImage,
            @RequestParam(value = "medicineImage", required = false) MultipartFile medicineImage) {

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
