package com.example.consultant_backend.mapper;

import com.example.consultant_backend.dto.user.UserResponseDTO;
import com.example.consultant_backend.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getGender(),
                user.getPreviousDisease(),
                user.getPhoneNumber(),
                user.getEmailVerified(),
                user.getCreatedAt()
        );
    }
}
