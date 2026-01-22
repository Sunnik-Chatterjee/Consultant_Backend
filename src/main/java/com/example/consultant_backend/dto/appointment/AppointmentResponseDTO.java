package com.example.consultant_backend.dto.appointment;

import com.example.consultant_backend.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDTO {

    private Long id;
    private Long userId;
    private String userName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status;  // PENDING, APPROVED, REJECTED, COMPLETED
    private String prescriptionImageUrl;
    private String medicineImageUrl;
}
