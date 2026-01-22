package com.example.consultant_backend.mapper;

import com.example.consultant_backend.dto.auth.AuthRequestDTO;
import com.example.consultant_backend.dto.auth.AuthResponseDTO;
import com.example.consultant_backend.model.Doctor;
import com.example.consultant_backend.model.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    // ========== USER MAPPING ==========

    public User toUserEntity(AuthRequestDTO request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setPreviousDisease(request.getPreviousDisease());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmailVerified(false);
        return user;
    }

    public AuthResponseDTO toAuthResponse(User user, String token) {
        return new AuthResponseDTO(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getImageUrl(),
                user.getPassword() != null,
                user.getGoogleId() != null
        );
    }

    // ========== DOCTOR MAPPING ==========

    public AuthResponseDTO toAuthResponse(Doctor doctor, String token) {
        return new AuthResponseDTO(
                token,
                doctor.getId(),
                doctor.getName(),
                doctor.getEmail(),
                doctor.getImageUrl(),
                doctor.getPassword() != null,
                doctor.getGoogleId() != null
        );
    }
}
