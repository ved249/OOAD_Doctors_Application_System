package com.geekster.DoctorsAppointmentApplication.repository;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.geekster.DoctorsAppointmentApplication.model.DoctorAvailability;

public interface IDoctorAvailabilityRepo extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctorDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);
    List<DoctorAvailability> findByDoctorDoctorId(Long doctorId);
    List<DoctorAvailability> findByDayOfWeekAndIsAvailableTrue(DayOfWeek dayOfWeek);
}
