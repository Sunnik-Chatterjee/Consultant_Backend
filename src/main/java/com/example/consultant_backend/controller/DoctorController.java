package com.example.consultant_backend.controller;


import com.example.consultant_backend.common.ApiResponse;
import com.example.consultant_backend.model.Doctor;
import com.example.consultant_backend.repo.DoctorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorRepo doctorRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Doctor>>> getAllDoctors() {
        return ResponseEntity.ok(
                ApiResponse.success("Doctors retrieved", doctorRepo.findAll())
        );
    }
}
