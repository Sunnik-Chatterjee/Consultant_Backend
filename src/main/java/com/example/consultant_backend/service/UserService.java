package com.example.consultant_backend.service;

import com.example.consultant_backend.dto.user.UpdateUserProfileDTO;
import com.example.consultant_backend.mapper.UserMapper;
import com.example.consultant_backend.model.Appointment;
import com.example.consultant_backend.model.User;
import com.example.consultant_backend.repo.AppointmentRepo;
import com.example.consultant_backend.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepo userRepo;
    private final AppointmentRepo appointmentRepo;
    private final UserMapper userMapper;

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        log.info("📋 Fetching all users");
        return userRepo.findAll();
    }

    /**
     * Get user by email (used after login)
     */
    public User getUserByEmail(String email) {
        log.info("🔍 Fetching user by email: {}", email);
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        log.info("🔍 Fetching user by ID: {}", userId);
        return userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    /**
     * Get user's appointments
     */
    public List<Appointment> getUserAppointments(Long userId) {
        log.info("📅 Fetching appointments for user ID: {}", userId);

        if (!userRepo.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        return appointmentRepo.findByUserId(userId);
    }

    /**
     * Get appointment images (prescription & medicine)
     */
    public Map<String, String> getAppointmentImages(Long appointmentId) {
        log.info("🖼️ Fetching images for appointment ID: {}", appointmentId);

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));

        Map<String, String> images = new HashMap<>();
        images.put("prescriptionUrl", appointment.getPrescriptionImageUrl());
        images.put("medicineUrl", appointment.getMedicineImageUrl());

        return images;
    }

    /**
     * Update user profile
     * Uses mapper to update only non-null fields
     */
    @Transactional
    public User updateUserProfile(Long userId, UpdateUserProfileDTO updateDTO) {
        log.info("✏️ Updating profile for user ID: {}", userId);

        User user = getUserById(userId);

        // Use mapper to update user entity
        userMapper.updateUserFromDTO(updateDTO, user);

        User savedUser = userRepo.save(user);
        log.info("✅ Profile updated successfully for user ID: {}", userId);

        return savedUser;
    }
}
