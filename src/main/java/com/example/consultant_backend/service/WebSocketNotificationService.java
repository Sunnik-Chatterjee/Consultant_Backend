package com.example.consultant_backend.service;

import com.example.consultant_backend.model.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void updateDoctorPendingList(Long doctorId, List<Appointment> appointments){
        String topic = "/topic/doctor/" + doctorId + "/pending";
        messagingTemplate.convertAndSend(topic, appointments);
        log.info("Updated pending list for doctor {}: {} appointments", doctorId, appointments.size());
    }
}
