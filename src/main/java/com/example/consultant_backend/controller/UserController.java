package com.example.consultant_backend.controller;

import com.example.consultant_backend.common.ApiResponse;
import com.example.consultant_backend.dto.user.UpdateUserProfileDTO;
import com.example.consultant_backend.model.Appointment;
import com.example.consultant_backend.model.User;
import com.example.consultant_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get current logged-in user's profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getCurrentUserProfile(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        log.info("📋 Get profile request from userId: {}", userId);

        User user = userService.getUserById(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User profile retrieved", user)
        );
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long userId) {
        log.info("🔍 Get user by ID: {}", userId);

        User user = userService.getUserById(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User retrieved", user)
        );
    }

    /**
     * Get all users (Admin only)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        log.info("📋 Get all users");

        List<User> users = userService.getAllUsers();

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved", users)
        );
    }

    /**
     * Get current user's appointments
     */
    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<List<Appointment>>> getCurrentUserAppointments(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        log.info("📅 Get appointments for userId: {}", userId);

        List<Appointment> appointments = userService.getUserAppointments(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Appointments retrieved", appointments)
        );
    }

    /**
     * Get appointment images
     */
    @GetMapping("/appointments/{appointmentId}/images")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAppointmentImages(
            @PathVariable Long appointmentId
    ) {
        log.info("🖼️ Get images for appointment: {}", appointmentId);

        Map<String, String> images = userService.getAppointmentImages(appointmentId);

        return ResponseEntity.ok(
                ApiResponse.success("Images retrieved", images)
        );
    }

    /**
     * Update current user's profile
     * Fields: name, age, gender, phoneNumber, previousDisease, imageUrl
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateCurrentUserProfile(
            Authentication authentication,
            @RequestBody UpdateUserProfileDTO updateDTO
    ) {
        Long userId = (Long) authentication.getPrincipal();

        log.info("✏️ Update profile request from userId: {}", userId);

        User updatedUser = userService.updateUserProfile(userId, updateDTO);

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", updatedUser)
        );
    }
}
