package com.example.consultant_backend.mapper;

import com.example.consultant_backend.dto.auth.AuthRequestDTO;
import com.example.consultant_backend.dto.auth.AuthResponseDTO;
import com.example.consultant_backend.model.User;
import org.springframework.stereotype.Component;
import com.example.consultant_backend.model.Doctor;

@Component
public class AuthMapper {

    public User toUserEntity(AuthRequestDTO request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // do NOT set password here, AuthService already encodes and sets it
        return user;
    }

    // existing for User
    public AuthResponseDTO toAuthResponse(User user, String token) {
        AuthResponseDTO dto = new AuthResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setProfileCompleted(isUserProfileCompleted(user));
        dto.setToken(token);
        return dto;
    }

    // new for Doctor
    public AuthResponseDTO toAuthResponse(Doctor doctor, String token) {
        AuthResponseDTO dto = new AuthResponseDTO();
        dto.setId(doctor.getId());
        dto.setName(doctor.getName());
        dto.setEmail(doctor.getEmail());
        // for doctors you probably always consider profile completed or add a similar flag if you want
        dto.setProfileCompleted(true);
        dto.setToken(token);
        return dto;
    }

    private boolean isUserProfileCompleted(User user) {
        return user.getAge() != null
                && user.getGender() != null && !user.getGender().isBlank()
                && user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank();
    }
}
