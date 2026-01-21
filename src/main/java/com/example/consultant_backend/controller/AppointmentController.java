// AppointmentController.java
package com.example.consultant_backend.controller;

import com.example.consultant_backend.common.ApiResponse;
import com.example.consultant_backend.model.Appointment;
import com.example.consultant_backend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/book")
    public ResponseEntity<ApiResponse<Appointment>> bookAppointment(
            @RequestParam Long userId,
            @RequestParam Long doctorId,
            @RequestParam String date,
            @RequestParam String time) {

        Appointment appointment = appointmentService.bookAppointment(
                userId, doctorId,
                LocalDate.parse(date),
                LocalTime.parse(time)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment booked successfully", appointment));
    }

    // User views their appointments
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Appointment>>> getUserAppointments(
            @PathVariable Long userId) {
        List<Appointment> appointments = appointmentService.getUserAppointments(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Appointments retrieved", appointments)
        );
    }

    // Get single appointment details
    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<Appointment>> getAppointment(
            @PathVariable Long appointmentId) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        return ResponseEntity.ok(
                ApiResponse.success("Appointment retrieved", appointment)
        );
    }


    // Doctor requests pending appointments via WebSocket
    @MessageMapping("/doctor/{doctorId}/get-pending")
    public void getDoctorPendingAppointments(@DestinationVariable Long doctorId) {
        List<Appointment> appointments = appointmentService.getPendingAppointments(doctorId);
        messagingTemplate.convertAndSend(
                "/topic/doctor/" + doctorId + "/pending",
                appointments
        );
    }

    // Doctor approves appointment via WebSocket
    @MessageMapping("/appointment/approve")
    public void approveAppointment(Map<String, Object> payload) {
        Long appointmentId = Long.parseLong(payload.get("appointmentId").toString());

        Appointment approved = appointmentService.approveAppointment(appointmentId);
        Long doctorId = approved.getDoctor().getId();

        // Send updated pending list to doctor in real-time
        List<Appointment> updatedList = appointmentService.getPendingAppointments(doctorId);
        messagingTemplate.convertAndSend(
                "/topic/doctor/" + doctorId + "/pending",
                updatedList
        );
    }

    // Doctor rejects appointment via WebSocket
    @MessageMapping("/appointment/reject")
    public void rejectAppointment(Map<String, Object> payload) {
        Long appointmentId = Long.parseLong(payload.get("appointmentId").toString());

        Appointment rejected = appointmentService.rejectAppointment(appointmentId);
        Long doctorId = rejected.getDoctor().getId();

        // Send updated pending list to doctor in real-time
        List<Appointment> updatedList = appointmentService.getPendingAppointments(doctorId);
        messagingTemplate.convertAndSend(
                "/topic/doctor/" + doctorId + "/pending",
                updatedList
        );
    }
}
