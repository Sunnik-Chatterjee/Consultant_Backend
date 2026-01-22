package com.example.consultant_backend.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String imageUrl;
    private String specialization;
}
