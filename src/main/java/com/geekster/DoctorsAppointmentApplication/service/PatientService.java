package com.geekster.DoctorsAppointmentApplication.service;

import com.geekster.DoctorsAppointmentApplication.dto.SignInInput;
import com.geekster.DoctorsAppointmentApplication.dto.SignInOutput;
import com.geekster.DoctorsAppointmentApplication.dto.SignUpInput;
import com.geekster.DoctorsAppointmentApplication.dto.SignUpOutput;
import com.geekster.DoctorsAppointmentApplication.model.AppointmentKey;
import com.geekster.DoctorsAppointmentApplication.model.AuthenticationToken;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.repository.IPatientRepo;
import jakarta.xml.bind.DatatypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class PatientService {

    @Autowired
    IPatientRepo patientRepo;

    @Autowired
    AuthenticationService tokenService;

    @Autowired
    DoctorService doctorService;

    @Autowired
    AppointmentService appointmentService;
    public SignUpOutput signUp(SignUpInput signUpDto) {

        //check if user exists or not based on email
        Patient patient = patientRepo.findFirstByPatientEmail(signUpDto.getUserEmail());

        if(patient != null)
        {
            throw new IllegalStateException("Patient already exists!!!!...sign in instead");
        }

//      encryption
        String encryptedPassword = null;

        try {
            encryptedPassword = encryptPassword(signUpDto.getUserPassword());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        patient = new Patient(signUpDto.getUserFirstName(), signUpDto.getUserLastName(),
                signUpDto.getUserEmail(), encryptedPassword , signUpDto.getUserContact());

        patientRepo.save(patient);

        return new SignUpOutput("Patient registered","Patient created successfully");

    }

    private String encryptPassword(String userPassword) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        md5.update(userPassword.getBytes());
        byte[] digested = md5.digest();

//        String hash = DatatypeConverter.printHexBinary(digested);
        String hash = DatatypeConverter.printHexBinary(digested);

        return hash;

    }

    public SignInOutput signIn(SignInInput signInDto) {
        //check if user exists or not based on email
        Patient patient = patientRepo.findFirstByPatientEmail(signInDto.getPatientEmail());

        if(patient == null)
        {
            throw new IllegalStateException("User invalid!!!!...sign up instead");
        }

        String encryptedPassword = null;

        try {
            encryptedPassword = encryptPassword(signInDto.getPatientPassword());
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        }

        //match it with database encrypted password

        boolean isPasswordValid = encryptedPassword.equals(patient.getPatientPassword());

        if(!isPasswordValid)
        {
            throw new IllegalStateException("User invalid!!!!...sign up instead");
        }

        AuthenticationToken token = new AuthenticationToken(patient);

        tokenService.saveToken(token);

        //set up output response

        return new SignInOutput("Authentication Successfull !!!", token.getToken());

    }

    public List<Doctor> getAllDoctors() {

        return doctorService.getAllDoctors();

    }

    public void cancelAppointment(AppointmentKey key) {

        appointmentService.cancelAppointment(key);

    }

    /**
     * Authenticate patient with email and password for MVC login
     * @param email patient email
     * @param password patient password (plain text)
     * @return Patient object if credentials are valid, null otherwise
     */
    public Patient authenticatePatient(String email, String password) {
        // Find patient by email
        Patient patient = patientRepo.findFirstByPatientEmail(email);

        if (patient == null) {
            return null; // Patient not found
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
        if (encryptedPassword.equals(patient.getPatientPassword())) {
            return patient; // Authentication successful
        }

        return null; // Password mismatch
    }

    /**
     * Sign up a patient from MVC form
     */
    public void signUpMvc(String firstName, String lastName, String email, String password, String contact) {
        Patient existingPatient = patientRepo.findFirstByPatientEmail(email);
        if (existingPatient != null) {
            throw new IllegalStateException("Email already registered");
        }

        String encryptedPassword = null;
        try {
            encryptedPassword = encryptPassword(password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Password encryption failed");
        }

        Patient newPatient = new Patient(firstName, lastName, email, encryptedPassword, contact);
        patientRepo.save(newPatient);
    }

    /**
     * Get patient by email
     */
    public Patient getPatientByEmail(String email) {
        return patientRepo.findFirstByPatientEmail(email);
    }

    /**
     * Book appointment from MVC form
     */
    public void bookAppointmentMvc(Long patientId, Long doctorId, String appointmentDateTime) {
        try {
            appointmentService.bookAppointmentMvc(patientId, doctorId, appointmentDateTime);
        } catch (Exception e) {
            throw new RuntimeException("Failed to book appointment: " + e.getMessage());
        }
    }
}