package com.example.consultant_backend.mapper;

import com.example.consultant_backend.dto.doctor.DoctorResponseDTO;
import com.example.consultant_backend.model.Doctor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DoctorMapper {

    public DoctorResponseDTO toResponseDTO(Doctor doctor) {
        return new DoctorResponseDTO(
                doctor.getId(),
                doctor.getName(),
                doctor.getEmail(),
                doctor.getPhoneNumber(),
                doctor.getImageUrl(),
                doctor.getSpecialization()
        );
    }

    public List<DoctorResponseDTO> toResponseDTOList(List<Doctor> doctors) {
        return doctors.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
