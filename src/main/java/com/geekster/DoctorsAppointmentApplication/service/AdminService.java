package com.geekster.DoctorsAppointmentApplication.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geekster.DoctorsAppointmentApplication.model.Admin;
import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.repository.IAdminRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IAppointmentRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IDoctorRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IPatientRepo;

@Service
public class AdminService {

    @Autowired
    IAdminRepo adminRepo;

    @Autowired
    IDoctorRepo doctorRepo;

    @Autowired
    IPatientRepo patientRepo;

    @Autowired
    IAppointmentRepo appointmentRepo;

    /**
     * Authenticate admin login
     */
    public Admin authenticateAdmin(String email, String password) {
        Admin admin = adminRepo.findByAdminEmail(email);
        if (admin != null && admin.getAdminPassword().equals(password)) {
            return admin;
        }
        return null;
    }

    // ========== DOCTOR MANAGEMENT ==========

    /**
     * Get all doctors
     */
    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    /**
     * Get doctor by ID
     */
    public Doctor getDoctorById(Long doctorId) {
        return doctorRepo.findByDoctorId(doctorId);
    }

    /**
     * Add new doctor
     */
    @Transactional
    public void addDoctor(Doctor doctor) {
        // Check if doctor with same email already exists
        Doctor existing = doctorRepo.findFirstByDoctorEmail(doctor.getDoctorEmail());
        if (existing != null) {
            throw new RuntimeException("Doctor with this email already exists");
        }
        doctorRepo.save(doctor);
    }

    /**
     * Update doctor details
     */
    @Transactional
    public void updateDoctor(Long doctorId, Doctor updatedDoctor) {
        Doctor doctor = doctorRepo.findByDoctorId(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor not found");
        }

        doctor.setDoctorName(updatedDoctor.getDoctorName());
        doctor.setSpecialization(updatedDoctor.getSpecialization());
        // Email and password should not be updated by admin
        doctorRepo.save(doctor);
    }

    /**
     * Delete doctor
     */
    @Transactional
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = doctorRepo.findByDoctorId(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor not found");
        }
        doctorRepo.deleteById(doctorId);
    }

    // ========== APPOINTMENT MONITORING ==========

    /**
     * Get all appointments
     */
    public List<Appointment> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    // ========== PATIENT MANAGEMENT ==========

    /**
     * Get all patients
     */
    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    /**
     * Delete patient
     */
    @Transactional
    public void deletePatient(Long patientId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        patientRepo.deleteById(patientId);
    }

    /**
     * Get patient by ID
     */
    public Patient getPatientById(Long patientId) {
        return patientRepo.findById(patientId).orElse(null);
    }
}
