package com.example.consultant_backend.config;

import com.example.consultant_backend.model.*;
import com.example.consultant_backend.repo.AppointmentRepo;
import com.example.consultant_backend.repo.DoctorRepo;
import com.example.consultant_backend.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final DoctorRepo doctorRepo;
    private final UserRepo userRepo;
    private final AppointmentRepo appointmentRepo;

    @Override
    public void run(String... args) throws Exception {
        if (doctorRepo.count() == 0) {
            seedDoctors();
        }

        if (userRepo.count() == 0) {
            seedUsers();
        }

        if (appointmentRepo.count() == 0) {
            seedAppointments();
        }
    }

    private void seedDoctors() {
        Doctor doctor1 = new Doctor();
        doctor1.setEmail("dr.sharma@hospital.com");
        doctor1.setName("Dr. Rajesh Sharma");
        doctor1.setPhoneNumber("+91-9876543210");
        doctor1.setPassword("doctor123"); // In production, use passwordEncoder
        doctor1.setImageUrl("https://randomuser.me/api/portraits/men/1.jpg");
        doctor1.setFcmToken(null); // Will be set when doctor logs in from mobile

        Doctor doctor2 = new Doctor();
        doctor2.setEmail("dr.priya@hospital.com");
        doctor2.setName("Dr. Priya Verma");
        doctor2.setPhoneNumber("+91-9876543211");
        doctor2.setPassword("doctor123");
        doctor2.setImageUrl("https://randomuser.me/api/portraits/women/2.jpg");
        doctor2.setFcmToken(null);

        doctorRepo.save(doctor1);
        doctorRepo.save(doctor2);
        log.info("✅ Seeded 2 doctors");
    }

    private void seedUsers() {
        User user1 = new User();
        user1.setEmail("amit.kumar@gmail.com");
        user1.setName("Amit Kumar");
        user1.setPassword("user123");
        user1.setGender(Gender.MALE);
        user1.setAge(28);
        user1.setPreviousDisease("Diabetes");
        user1.setImageUrl("https://randomuser.me/api/portraits/men/10.jpg");
        user1.setFcmToken(null);

        User user2 = new User();
        user2.setEmail("neha.singh@gmail.com");
        user2.setName("Neha Singh");
        user2.setPassword("user123");
        user2.setGender(Gender.FEMALE);
        user2.setAge(32);
        user2.setPreviousDisease("Hypertension");
        user2.setImageUrl("https://randomuser.me/api/portraits/women/20.jpg");
        user2.setFcmToken(null);

        User user3 = new User();
        user3.setEmail("rohit.patel@gmail.com");
        user3.setName("Rohit Patel");
        user3.setPassword("user123");
        user3.setGender(Gender.MALE);
        user3.setAge(45);
        user3.setPreviousDisease("Asthma");
        user3.setImageUrl("https://randomuser.me/api/portraits/men/30.jpg");
        user3.setFcmToken(null);

        userRepo.save(user1);
        userRepo.save(user2);
        userRepo.save(user3);
        log.info("✅ Seeded 3 users");
    }

    private void seedAppointments() {
        Doctor doctor = doctorRepo.findById(1L).orElseThrow();
        User user1 = userRepo.findById(1L).orElseThrow();
        User user2 = userRepo.findById(2L).orElseThrow();
        User user3 = userRepo.findById(3L).orElseThrow();

        // Pending appointment 1
        Appointment apt1 = new Appointment();
        apt1.setDoctor(doctor);
        apt1.setUser(user1);
        apt1.setAppointmentDate(LocalDate.now().plusDays(2));
        apt1.setAppointmentTime(LocalTime.of(10, 30));
        apt1.setStatus(AppointmentStatus.PENDING);

        // Pending appointment 2
        Appointment apt2 = new Appointment();
        apt2.setDoctor(doctor);
        apt2.setUser(user2);
        apt2.setAppointmentDate(LocalDate.now().plusDays(3));
        apt2.setAppointmentTime(LocalTime.of(14, 0));
        apt2.setStatus(AppointmentStatus.PENDING);

        // Approved appointment
        Appointment apt3 = new Appointment();
        apt3.setDoctor(doctor);
        apt3.setUser(user3);
        apt3.setAppointmentDate(LocalDate.now().plusDays(5));
        apt3.setAppointmentTime(LocalTime.of(11, 0));
        apt3.setStatus(AppointmentStatus.APPROVED);

        appointmentRepo.save(apt1);
        appointmentRepo.save(apt2);
        appointmentRepo.save(apt3);
        log.info("✅ Seeded 3 appointments (2 pending, 1 approved)");
    }
}
