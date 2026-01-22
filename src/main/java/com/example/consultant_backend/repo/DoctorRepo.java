package com.example.consultant_backend.repo;

import com.example.consultant_backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepo extends JpaRepository<Doctor,Long> {
    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByGoogleId(String googleId);
    Boolean existsByEmail(String email);
}
