package com.geekster.DoctorsAppointmentApplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.geekster.DoctorsAppointmentApplication.model.Admin;

public interface IAdminRepo extends JpaRepository<Admin, Long> {

    Admin findByAdminEmail(String adminEmail);

    Admin findByAdminId(Long adminId);
}
