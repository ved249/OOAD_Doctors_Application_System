package com.geekster.DoctorsAppointmentApplication.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.AppointmentKey;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.DoctorAvailability;
import com.geekster.DoctorsAppointmentApplication.model.Specialization;
import com.geekster.DoctorsAppointmentApplication.repository.IDoctorAvailabilityRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IDoctorRepo;

import jakarta.xml.bind.DatatypeConverter;

@Service
public class DoctorService {

    @Autowired
    IDoctorRepo doctorRepo;

    @Autowired
    IDoctorAvailabilityRepo availabilityRepo;

    @Autowired
    AppointmentService appointmentService;

    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    /**
     * Filter doctors by specialization
     */
    public List<Doctor> getDoctorsBySpecialization(Specialization specialization) {
        return doctorRepo.findBySpecialization(specialization);
    }

    /**
     * Search doctors by name
     */
    public List<Doctor> searchDoctorsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllDoctors();
        }
        return doctorRepo.findByDoctorNameContainingIgnoreCase(name);
    }

    /**
     * Set doctor availability for a specific day
     */
    @Transactional
    public void setAvailability(Long doctorId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        Doctor doctor = doctorRepo.findByDoctorId(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor not found");
        }

        // Check if availability already exists for this day
        List<DoctorAvailability> existing = availabilityRepo.findByDoctorDoctorIdAndDayOfWeek(doctorId, dayOfWeek);
        
        if (!existing.isEmpty()) {
            // Update existing
            DoctorAvailability availability = existing.get(0);
            availability.setStartTime(startTime);
            availability.setEndTime(endTime);
            availabilityRepo.save(availability);
        } else {
            // Create new
            DoctorAvailability availability = new DoctorAvailability(doctor, dayOfWeek, startTime, endTime);
            availabilityRepo.save(availability);
        }
    }

    /**
     * Get doctor availability
     */
    public List<DoctorAvailability> getDoctorAvailability(Long doctorId) {
        return availabilityRepo.findByDoctorDoctorId(doctorId);
    }

    /**
     * Check if doctor is available on a specific day
     */
    public boolean isDoctorAvailableOnDay(Long doctorId, DayOfWeek dayOfWeek) {
        List<DoctorAvailability> availability = availabilityRepo.findByDoctorDoctorIdAndDayOfWeek(doctorId, dayOfWeek);
        return !availability.isEmpty() && availability.get(0).getAvailable();
    }


    public void addDoctor(Doctor doctor) {

        doctorRepo.save(doctor);

    }

    public List<Appointment> getMyAppointments(Long docId) {

        Doctor myDoc = doctorRepo.findByDoctorId(docId);

        if(myDoc == null)
        {
            throw new IllegalStateException("The doctor does not exist");
        }

        return myDoc.getAppointments();
    }

    private String encryptPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(password.getBytes());
        byte[] digested = md5.digest();
        return DatatypeConverter.printHexBinary(digested);
    }

    /**
     * Authenticate doctor with email and password for MVC login
     * @param email doctor email
     * @param password doctor password (plain text)
     * @return Doctor object if credentials are valid, null otherwise
     */
    public Doctor authenticateDoctor(String email, String password) {
        // Find doctor by email
        Doctor doctor = doctorRepo.findFirstByDoctorEmail(email);

        if (doctor == null) {
            return null; // Doctor not found
        }

        // Encrypt the provided password and compare with stored password
        String encryptedPassword = null;
        try {
            encryptedPassword = encryptPassword(password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        // Verify password matches
        if (encryptedPassword.equals(doctor.getDoctorPassword())) {
            return doctor; // Authentication successful
        }

        return null; // Password mismatch
    }

    /**
     * Approve an appointment
     */
    @Transactional
    public void approveAppointment(AppointmentKey appointmentKey) {
        appointmentService.approveAppointment(appointmentKey);
    }

    /**
     * Reject an appointment
     */
    @Transactional
    public void rejectAppointment(AppointmentKey appointmentKey) {
        appointmentService.rejectAppointment(appointmentKey);
    }
}