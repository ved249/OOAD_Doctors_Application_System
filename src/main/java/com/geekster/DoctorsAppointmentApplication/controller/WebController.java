package com.geekster.DoctorsAppointmentApplication.controller;

import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @Autowired
    PatientService patientService;

    // ---------- LOGIN PAGE ----------
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // loads login.html
    }

    // ---------- LOGIN SUBMIT ----------
    @PostMapping("/login")
    public String loginSubmit(
            @RequestParam String email,
            @RequestParam String password,
            Model model
    ) {
        // Authenticate using PatientService with database
        Patient patient = patientService.authenticatePatient(email, password);

        if (patient != null) {
            // Login successful - redirect to book-appointment
            model.addAttribute("patientId", patient.getPatientId());
            model.addAttribute("patientName", patient.getPatientFirstName() + " " + patient.getPatientLastName());
            return "redirect:/book-appointment";
        }

        // Login failed - show error message and stay on login page
        model.addAttribute("error", "Invalid email or password. Please try again.");
        return "login";
    }

    // ---------- BOOK APPOINTMENT PAGE ----------
    @GetMapping("/book-appointment")
    public String bookAppointmentPage() {
        return "book-appointment"; // loads book-appointment.html
    }

    // ---------- BOOK APPOINTMENT SUBMIT ----------
    @PostMapping("/book-appointment")
    public String bookAppointmentSubmit(
            @RequestParam String patientName,
            @RequestParam String doctorName,
            @RequestParam String time,
            Model model
    ) {
        model.addAttribute("message",
                "Appointment booked successfully for " + patientName +
                " with Dr. " + doctorName + " at " + time
        );

        return "book-appointment";
    }
}

