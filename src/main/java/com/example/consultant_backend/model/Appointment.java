package com.example.consultant_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne (optional = false)
    private Doctor doctor;
    @ManyToOne(optional = false)
    private User user;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    AppointmentStatus status;
    private String prescriptionImageUrl;
    private String medicineImageUrl;

}
