package com.example.consultant_backend.controller;

import com.example.consultant_backend.common.ApiResponse;
import com.example.consultant_backend.model.Appointment;
import com.example.consultant_backend.model.User;
import com.example.consultant_backend.repo.AppointmentRepo;
import com.example.consultant_backend.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AppointmentRepo appointmentRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved", userRepo.findAll())
        );
    }

    @GetMapping("/appointment/{appointmentId}/images")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAppointmentImages(@PathVariable Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElse(null);

        if (appointment == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.error("Appointment not found")
            );
        }

        Map<String, String> images = new HashMap<>();
        images.put("prescriptionUrl", appointment.getPrescriptionImageUrl());
        images.put("medicineUrl", appointment.getMedicineImageUrl());

        return ResponseEntity.ok(
                ApiResponse.success("Images retrieved successfully", images)
        );
    }
}
