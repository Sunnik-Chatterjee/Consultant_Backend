package com.example.consultant_backend.mapper;

import com.example.consultant_backend.dto.appointment.AppointmentResponseDTO;
import com.example.consultant_backend.model.Appointment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppointmentMapper {

    public AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getUser().getId(),
                appointment.getUser().getName(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),
                appointment.getDoctor().getSpecialization(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus(),
                appointment.getPrescriptionImageUrl(),
                appointment.getMedicineImageUrl()
        );
    }

    public List<AppointmentResponseDTO> toResponseDTOList(List<Appointment> appointments) {
        return appointments.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
