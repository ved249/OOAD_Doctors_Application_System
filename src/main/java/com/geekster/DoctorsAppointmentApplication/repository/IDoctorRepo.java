package com.geekster.DoctorsAppointmentApplication.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Specialization;

public interface IDoctorRepo extends JpaRepository<Doctor, Long> {

    Doctor findByDoctorId(Long docId);

    Doctor findFirstByDoctorEmail(String doctorEmail);

    // Filter by specialization
    List<Doctor> findBySpecialization(Specialization specialization);

    // Search by name
    List<Doctor> findByDoctorNameContainingIgnoreCase(String name);
}
