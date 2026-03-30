package com.geekster.DoctorsAppointmentApplication.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geekster.DoctorsAppointmentApplication.dto.DoctorSignUpInput;
import com.geekster.DoctorsAppointmentApplication.dto.DoctorSignUpOutput;
import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.AppointmentKey;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.DoctorAvailability;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
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

    public void saveAppointmentNotes(AppointmentKey appointmentKey, String diagnosis, String prescription, String doctorNotes) {
        appointmentService.saveAppointmentNotes(appointmentKey, diagnosis, prescription, doctorNotes);
    }

    public List<Appointment> getMyPastAppointments(Long docId) {
        Doctor doctor = doctorRepo.findByDoctorId(docId);
        if (doctor == null) {
            throw new IllegalStateException("Doctor not found");
        }
        return doctor.getAppointments().stream()
                .filter(appointment -> appointment.getId().getTime().isBefore(java.time.LocalDateTime.now()))
                .toList();
    }

    public String buildPatientReport(Long patientId) {
        return appointmentService.buildPatientHistoryExport(patientId);
    }

    /**
     * Doctor signup
     */
    public DoctorSignUpOutput doctorSignUp(DoctorSignUpInput signUpDto) {
        // Check if doctor exists or not based on email
        Doctor doctor = doctorRepo.findFirstByDoctorEmail(signUpDto.getDoctorEmail());

        if (doctor != null) {
            throw new IllegalStateException("Doctor already exists!!!!...sign in instead");
        }

        // Encryption
        String encryptedPassword = null;
        try {
            encryptedPassword = encryptPassword(signUpDto.getDoctorPassword());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        doctor = new Doctor(signUpDto.getDoctorName(), signUpDto.getDoctorEmail(), encryptedPassword, signUpDto.getSpecialization());

        doctorRepo.save(doctor);

        return new DoctorSignUpOutput("Doctor registered", "Doctor created successfully");
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
        System.out.println("Authenticating doctor: " + email);
        // Find doctor by email
        Doctor doctor = doctorRepo.findFirstByDoctorEmail(email);

        if (doctor == null) {
            System.out.println("Doctor not found for email: " + email);
            return null; // Doctor not found
        }

        System.out.println("Doctor found: " + doctor.getDoctorName());
        // Encrypt the provided password and compare with stored password
        String encryptedPassword = null;
        try {
            encryptedPassword = encryptPassword(password);
            System.out.println("Provided password hash: " + encryptedPassword);
            System.out.println("Stored password hash: " + doctor.getDoctorPassword());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        // Verify password matches
        if (encryptedPassword.equals(doctor.getDoctorPassword())) {
            System.out.println("Authentication successful");
            return doctor; // Authentication successful
        }

        System.out.println("Password mismatch");
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

    /**
     * Get patients associated with this doctor (from appointments)
     */
    public List<Patient> getMyPatients(Long doctorId) {
        Doctor doctor = doctorRepo.findByDoctorId(doctorId);
        if (doctor == null) {
            throw new IllegalStateException("Doctor not found");
        }
        return doctor.getAppointments().stream()
            .map(Appointment::getPatient)
            .distinct()
            .collect(Collectors.toList());
    }
}