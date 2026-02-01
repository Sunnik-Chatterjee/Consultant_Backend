package com.example.consultant_backend.mapper;

import com.example.consultant_backend.dto.user.UpdateUserProfileDTO;
import com.example.consultant_backend.dto.user.UserResponseDTO;
import com.example.consultant_backend.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserMapper {

    /**
     * Convert User entity to response DTO
     */
    public UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getGender(),
                user.getPreviousDisease(),
                user.getPhoneNumber(),
                isProfileCompleted(user)
        );
    }

    /**
     * Convert list of User entities to list of response DTOs
     */
    public List<UserResponseDTO> toResponseDTOList(List<User> users) {
        return users.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update User entity from DTO (only non-null fields)
     * Used for profile updates by users themselves
     */
    public void updateUserFromDTO(UpdateUserProfileDTO dto, User user) {
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName().trim());
            log.debug("Updated name to: {}", dto.getName());
        }

        if (dto.getAge() != null && dto.getAge() > 0) {
            user.setAge(dto.getAge());
            log.debug("Updated age to: {}", dto.getAge());
        }

        if (dto.getGender() != null && !dto.getGender().trim().isEmpty()) {
            user.setGender(dto.getGender().trim());
            log.debug("Updated gender to: {}", dto.getGender());
        }

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(dto.getPhoneNumber().trim());
            log.debug("Updated phone number");
        }

        if (dto.getPreviousDisease() != null) {
            // Allow empty string to clear previous disease
            String trimmed = dto.getPreviousDisease().trim();
            user.setPreviousDisease(trimmed.isEmpty() ? null : trimmed);
            log.debug("Updated previous disease");
        }

        // imageUrl intentionally ignored for v1 (not in DTO/entity usage now)
    }

    private boolean isProfileCompleted(User user) {
        return user.getAge() != null
                && user.getGender() != null && !user.getGender().isBlank()
                && user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank();
    }
}
