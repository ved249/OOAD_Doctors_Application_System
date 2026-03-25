package com.geekster.DoctorsAppointmentApplication.controller;

import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.service.DoctorService;
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
@RequestMapping("/doctor")
public class DoctorMvcController {

    @Autowired
    DoctorService doctorService;

    // ---------- DOCTOR LOGIN PAGE ----------
    @GetMapping("/login")
    public String doctorLoginPage() {
        return "doctor-login";
    }

    // ---------- DOCTOR LOGIN SUBMIT ----------
    @PostMapping("/login")
    public String doctorLoginSubmit(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        Doctor doctor = doctorService.authenticateDoctor(email, password);

        if (doctor != null) {
            // Login successful - store in session and redirect to appointments
            session.setAttribute("doctorId", doctor.getDoctorId());
            session.setAttribute("doctorName", doctor.getDoctorName());
            session.setAttribute("doctorEmail", doctor.getDoctorEmail());
            return "redirect:/doctor/appointments";
        }

        // Login failed - show error message
        model.addAttribute("error", "Invalid email or password. Please try again.");
        return "doctor-login";
    }

    // ---------- VIEW DOCTOR APPOINTMENTS PAGE ----------
    @GetMapping("/appointments")
    public String viewAppointments(HttpSession session, Model model) {
        Long doctorId = (Long) session.getAttribute("doctorId");
        if (doctorId == null) {
            return "redirect:/doctor/login";
        }

        try {
            List<Appointment> appointments = doctorService.getMyAppointments(doctorId);
            model.addAttribute("appointments", appointments);
            model.addAttribute("doctorName", session.getAttribute("doctorName"));
            return "doctor-appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading appointments: " + e.getMessage());
            return "doctor-appointments";
        }
    }

    // ---------- LOGOUT ----------
    @GetMapping("/logout")
    public String doctorLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
