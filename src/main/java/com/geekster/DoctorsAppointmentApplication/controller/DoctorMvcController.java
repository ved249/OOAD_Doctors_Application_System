package com.geekster.DoctorsAppointmentApplication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.geekster.DoctorsAppointmentApplication.dto.DoctorSignUpInput;
import com.geekster.DoctorsAppointmentApplication.dto.DoctorSignUpOutput;
import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.service.DoctorService;

import jakarta.servlet.http.HttpSession;

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

    // ---------- DOCTOR SIGNUP PAGE ----------
    @GetMapping("/signup")
    public String doctorSignupPage(Model model) {
        model.addAttribute("specializations", com.geekster.DoctorsAppointmentApplication.model.Specialization.values());
        return "doctor-signup";
    }

    // ---------- DOCTOR SIGNUP SUBMIT ----------
    @PostMapping("/signup")
    public String doctorSignupSubmit(
            @RequestParam String doctorName,
            @RequestParam String doctorEmail,
            @RequestParam String doctorPassword,
            @RequestParam com.geekster.DoctorsAppointmentApplication.model.Specialization specialization,
            Model model
    ) {
        try {
            DoctorSignUpInput signUpInput = new DoctorSignUpInput(doctorName, doctorEmail, doctorPassword, specialization);
            DoctorSignUpOutput signUpOutput = doctorService.doctorSignUp(signUpInput);
            model.addAttribute("message", signUpOutput.getMessage());
            return "doctor-signup-success";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("specializations", com.geekster.DoctorsAppointmentApplication.model.Specialization.values());
            return "doctor-signup";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during signup. Please try again.");
            model.addAttribute("specializations", com.geekster.DoctorsAppointmentApplication.model.Specialization.values());
            return "doctor-signup";
        }
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

    // ---------- APPROVE APPOINTMENT ----------
    @PostMapping("/approve-appointment")
    public String approveAppointment(
            @RequestParam Long appointmentId,
            @RequestParam String appointmentTime,
            HttpSession session,
            Model model
    ) {
        Long doctorId = (Long) session.getAttribute("doctorId");
        if (doctorId == null) {
            return "redirect:/doctor/login";
        }

        try {
            java.time.LocalDateTime time = java.time.LocalDateTime.parse(appointmentTime);
            com.geekster.DoctorsAppointmentApplication.model.AppointmentKey key = 
                new com.geekster.DoctorsAppointmentApplication.model.AppointmentKey(appointmentId, time);
            doctorService.approveAppointment(key);
            model.addAttribute("message", "Appointment approved successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to approve appointment: " + e.getMessage());
        }

        return "redirect:/doctor/appointments";
    }

    // ---------- REJECT APPOINTMENT ----------
    @PostMapping("/reject-appointment")
    public String rejectAppointment(
            @RequestParam Long appointmentId,
            @RequestParam String appointmentTime,
            HttpSession session,
            Model model
    ) {
        Long doctorId = (Long) session.getAttribute("doctorId");
        if (doctorId == null) {
            return "redirect:/doctor/login";
        }

        try {
            java.time.LocalDateTime time = java.time.LocalDateTime.parse(appointmentTime);
            com.geekster.DoctorsAppointmentApplication.model.AppointmentKey key = 
                new com.geekster.DoctorsAppointmentApplication.model.AppointmentKey(appointmentId, time);
            doctorService.rejectAppointment(key);
            model.addAttribute("message", "Appointment rejected successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to reject appointment: " + e.getMessage());
        }

        return "redirect:/doctor/appointments";
    }

    // ---------- VIEW MY PATIENTS ----------
    @GetMapping("/patients")
    public String viewMyPatients(HttpSession session, Model model) {
        Long doctorId = (Long) session.getAttribute("doctorId");
        if (doctorId == null) {
            return "redirect:/doctor/login";
        }

        try {
            List<Patient> patients = doctorService.getMyPatients(doctorId);
            model.addAttribute("patients", patients);
            model.addAttribute("doctorName", session.getAttribute("doctorName"));
            return "doctor-view-patients";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading patients: " + e.getMessage());
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
