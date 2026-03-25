package com.geekster.DoctorsAppointmentApplication.controller;

import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.service.DoctorService;
import com.geekster.DoctorsAppointmentApplication.service.PatientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientMvcController {

    @Autowired
    PatientService patientService;

    @Autowired
    DoctorService doctorService;

    // ---------- PATIENT SIGNUP PAGE ----------
    @GetMapping("/signup")
    public String patientSignupPage() {
        return "patient-signup";
    }

    // ---------- PATIENT SIGNUP SUBMIT ----------
    @PostMapping("/signup")
    public String patientSignupSubmit(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String contact,
            Model model
    ) {
        try {
            Patient existingPatient = patientService.getPatientByEmail(email);
            if (existingPatient != null) {
                model.addAttribute("error", "Email already registered. Please use a different email.");
                return "patient-signup";
            }

            Patient newPatient = new Patient(firstName, lastName, email, "", contact);
            // Password will be encrypted in the service
            patientService.signUpMvc(firstName, lastName, email, password, contact);
            model.addAttribute("success", "Signup successful! Please login now.");
            return "redirect:/patient/login";
        } catch (Exception e) {
            model.addAttribute("error", "Signup failed: " + e.getMessage());
            return "patient-signup";
        }
    }

    // ---------- PATIENT LOGIN PAGE ----------
    @GetMapping("/login")
    public String patientLoginPage() {
        return "patient-login";
    }

    // ---------- PATIENT LOGIN SUBMIT ----------
    @PostMapping("/login")
    public String patientLoginSubmit(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        Patient patient = patientService.authenticatePatient(email, password);

        if (patient != null) {
            // Login successful - store in session and redirect to book-appointment
            session.setAttribute("patientId", patient.getPatientId());
            session.setAttribute("patientName", patient.getPatientFirstName() + " " + patient.getPatientLastName());
            session.setAttribute("patientEmail", patient.getPatientEmail());
            return "redirect:/patient/book-appointment";
        }

        // Login failed - show error message
        model.addAttribute("error", "Invalid email or password. Please try again.");
        return "patient-login";
    }

    // ---------- BOOK APPOINTMENT PAGE ----------
    @GetMapping("/book-appointment")
    public String bookAppointmentPage(HttpSession session, Model model) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/patient/login";
        }

        // Get all doctors for dropdown
        List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors);
        return "patient-book-appointment";
    }

    // ---------- BOOK APPOINTMENT SUBMIT ----------
    @PostMapping("/book-appointment")
    public String bookAppointmentSubmit(
            @RequestParam Long doctorId,
            @RequestParam String appointmentDateTime,
            HttpSession session,
            Model model
    ) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/patient/login";
        }

        try {
            patientService.bookAppointmentMvc(patientId, doctorId, appointmentDateTime);
            model.addAttribute("message", "Appointment booked successfully!");
            model.addAttribute("doctors", doctorService.getAllDoctors());
            return "patient-book-appointment";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to book appointment: " + e.getMessage());
            model.addAttribute("doctors", doctorService.getAllDoctors());
            return "patient-book-appointment";
        }
    }

    // ---------- LOGOUT ----------
    @GetMapping("/logout")
    public String patientLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
