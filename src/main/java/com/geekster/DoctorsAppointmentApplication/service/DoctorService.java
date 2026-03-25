package com.geekster.DoctorsAppointmentApplication.service;

import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.repository.IDoctorRepo;
import jakarta.xml.bind.DatatypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class DoctorService {

    @Autowired
    IDoctorRepo doctorRepo;

    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
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
}