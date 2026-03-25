package com.geekster.DoctorsAppointmentApplication.repository;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.AppointmentKey;
import com.geekster.DoctorsAppointmentApplication.model.AppointmentStatus;

public interface IAppointmentRepo extends JpaRepository<Appointment, AppointmentKey> {
    
    // Find appointments by patient ID
    @Query("SELECT a FROM Appointment a WHERE a.patient.patientId = :patientId")
    List<Appointment> findByPatientId(@Param("patientId") Long patientId);

    // Find appointments by patient ID and status
    @Query("SELECT a FROM Appointment a WHERE a.patient.patientId = :patientId AND a.status = :status")
    List<Appointment> findByPatientIdAndStatus(@Param("patientId") Long patientId, @Param("status") AppointmentStatus status);

    // Find past appointments by patient ID (before current time)
    @Query("SELECT a FROM Appointment a WHERE a.patient.patientId = :patientId AND a.id.time < CURRENT_TIMESTAMP ORDER BY a.id.time DESC")
    List<Appointment> findPastAppointmentsByPatientId(@Param("patientId") Long patientId);

    // Find upcoming appointments by patient ID (after current time)
    @Query("SELECT a FROM Appointment a WHERE a.patient.patientId = :patientId AND a.id.time > CURRENT_TIMESTAMP ORDER BY a.id.time ASC")
    List<Appointment> findUpcomingAppointmentsByPatientId(@Param("patientId") Long patientId);

    // Find appointments in date range
    @Query("SELECT a FROM Appointment a WHERE a.patient.patientId = :patientId AND a.id.time BETWEEN :startDate AND :endDate")
    List<Appointment> findAppointmentsByDateRange(@Param("patientId") Long patientId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}

