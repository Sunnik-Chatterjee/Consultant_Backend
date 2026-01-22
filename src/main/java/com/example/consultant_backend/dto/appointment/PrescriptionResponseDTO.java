package com.example.consultant_backend.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponseDTO {

    private Long appointmentId;
    private String prescriptionImageUrl;
    private String medicineImageUrl;
}
