package com.example.consultant_backend.mapper;

import com.example.consultant_backend.dto.appointment.PrescriptionResponseDTO;
import com.example.consultant_backend.model.Appointment;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionMapper {

    public PrescriptionResponseDTO toResponseDTO(Appointment appointment) {
        return new PrescriptionResponseDTO(
                appointment.getId(),
                appointment.getPrescriptionImageUrl(),
                appointment.getMedicineImageUrl()
        );
    }
}
