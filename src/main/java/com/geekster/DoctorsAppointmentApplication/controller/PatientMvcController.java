package com.geekster.DoctorsAppointmentApplication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.service.AppointmentService;
import com.geekster.DoctorsAppointmentApplication.service.DoctorService;
import com.geekster.DoctorsAppointmentApplication.service.PatientService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/patient")
public class PatientMvcController {

    @Autowired
    PatientService patientService;

    @Autowired
    DoctorService doctorService;

    @Autowired
    AppointmentService appointmentService;

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
    public String bookAppointmentPage(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String searchName,
            HttpSession session, 
            Model model) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/patient/login";
        }

        // Get doctors based on filter
        List<Doctor> doctors;
        if (specialization != null && !specialization.isEmpty()) {
            doctors = patientService.getDoctorsBySpecialization(specialization);
            model.addAttribute("selectedSpecialization", specialization);
        } else if (searchName != null && !searchName.isEmpty()) {
            doctors = patientService.searchDoctors(searchName);
            model.addAttribute("searchName", searchName);
        } else {
            doctors = doctorService.getAllDoctors();
        }

        model.addAttribute("doctors", doctors);
        // Add all specializations for filter dropdown
        model.addAttribute("specializations", com.geekster.DoctorsAppointmentApplication.model.Specialization.values());
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
            model.addAttribute("specializations", com.geekster.DoctorsAppointmentApplication.model.Specialization.values());
            return "patient-book-appointment";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to book appointment: " + e.getMessage());
            model.addAttribute("doctors", doctorService.getAllDoctors());
            model.addAttribute("specializations", com.geekster.DoctorsAppointmentApplication.model.Specialization.values());
            return "patient-book-appointment";
        }
    }

    // ---------- VIEW APPOINTMENTS PAGE ----------
    @GetMapping("/my-appointments")
    public String viewMyAppointments(HttpSession session, Model model) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/patient/login";
        }

        try {
            List<com.geekster.DoctorsAppointmentApplication.model.Appointment> appointments = 
                patientService.getPatientAppointments(patientId);
            model.addAttribute("appointments", appointments);
            model.addAttribute("patientName", session.getAttribute("patientName"));
            return "patient-appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading appointments: " + e.getMessage());
            return "patient-appointments";
        }
    }

    // ---------- CANCEL APPOINTMENT ----------
    @PostMapping("/cancel-appointment")
    public String cancelAppointment(
            @RequestParam Long appointmentId,
            @RequestParam String appointmentTime,
            HttpSession session,
            Model model
    ) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/patient/login";
        }

        try {
            java.time.LocalDateTime time = java.time.LocalDateTime.parse(appointmentTime);
            patientService.cancelAppointmentMvc(patientId, appointmentId, time);
            model.addAttribute("message", "Appointment cancelled successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to cancel appointment: " + e.getMessage());
        }

        return "redirect:/patient/my-appointments";
    }

    // ---------- RESCHEDULE APPOINTMENT PAGE ----------
    @GetMapping("/reschedule-appointment")
    public String rescheduleAppointmentPage(
            @RequestParam Long appointmentId,
            @RequestParam String appointmentTime,
            HttpSession session,
            Model model
    ) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/patient/login";
        }

        try {
            List<Doctor> doctors = doctorService.getAllDoctors();
            model.addAttribute("doctors", doctors);
            model.addAttribute("appointmentId", appointmentId);
            model.addAttribute("appointmentTime", appointmentTime);
            return "patient-reschedule-appointment";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading reschedule form: " + e.getMessage());
            return "patient-appointments";
        }
    }

    // ---------- RESCHEDULE APPOINTMENT SUBMIT ----------
    @PostMapping("/reschedule-appointment")
    public String rescheduleAppointmentSubmit(
            @RequestParam Long appointmentId,
            @RequestParam String oldAppointmentTime,
            @RequestParam String newAppointmentDateTime,
            HttpSession session,
            Model model
    ) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/patient/login";
        }

        try {
            java.time.LocalDateTime oldTime = java.time.LocalDateTime.parse(oldAppointmentTime);
            patientService.rescheduleAppointmentMvc(patientId, appointmentId, oldTime, newAppointmentDateTime);
            model.addAttribute("message", "Appointment rescheduled successfully!");
            return "redirect:/patient/my-appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to reschedule appointment: " + e.getMessage());
            List<Doctor> doctors = doctorService.getAllDoctors();
            model.addAttribute("doctors", doctors);
            model.addAttribute("appointmentId", appointmentId);
            model.addAttribute("appointmentTime", oldAppointmentTime);
            return "patient-reschedule-appointment";
        }
    }

    // ---------- APPOINTMENT HISTORY & FILTERING ----------
    @GetMapping("/upcoming-appointments")
    public String viewUpcomingAppointments(HttpSession session, Model model) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/patient/login";
        }

        try {
            List<com.geekster.DoctorsAppointmentApplication.model.Appointment> appointments = 
                patientService.getUpcomingAppointments(patientId);
            model.addAttribute("appointments", appointments);
            model.addAttribute("appointmentType", "Upcoming");
            model.addAttribute("patientName", session.getAttribute("patientName"));
            return "patient-appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading upcoming appointments: " + e.getMessage());
            return "patient-appointments";
        }
    }

    @GetMapping("/past-appointments")
    public String viewPastAppointments(HttpSession session, Model model) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/patient/login";
        }

        try {
            List<com.geekster.DoctorsAppointmentApplication.model.Appointment> appointments = 
                patientService.getPastAppointments(patientId);
            model.addAttribute("appointments", appointments);
            model.addAttribute("appointmentType", "Past");
            model.addAttribute("patientName", session.getAttribute("patientName"));
            return "patient-appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading past appointments: " + e.getMessage());
            return "patient-appointments";
        }
    }

    @GetMapping("/appointments-by-date")
    public String filterAppointmentsByDate(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session,
            Model model) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/patient/login";
        }

        try {
            List<com.geekster.DoctorsAppointmentApplication.model.Appointment> appointments;
            
            if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDate + "T00:00:00");
                java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDate + "T23:59:59");
                appointments = appointmentService.getAppointmentsByDateRange(patientId, start, end);
                model.addAttribute("filterApplied", true);
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
            } else {
                appointments = patientService.getPatientAppointments(patientId);
                model.addAttribute("filterApplied", false);
            }
            
            model.addAttribute("appointments", appointments);
            model.addAttribute("appointmentType", "Filtered");
            model.addAttribute("patientName", session.getAttribute("patientName"));
            return "patient-appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Error filtering appointments: " + e.getMessage());
            return "patient-appointments";
        }
    }

    @GetMapping("/history")
    public String patientHistory(HttpSession session, Model model) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/patient/login";
        }

        try {
            List<com.geekster.DoctorsAppointmentApplication.model.Appointment> appointments = patientService.getPatientHistory(patientId);
            model.addAttribute("appointments", appointments);
            model.addAttribute("appointmentType", "History");
            model.addAttribute("patientName", session.getAttribute("patientName"));
            return "patient-appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading medical history: " + e.getMessage());
            return "patient-appointments";
        }
    }

    @GetMapping("/download-history")
    public org.springframework.http.ResponseEntity<byte[]> downloadHistory(HttpSession session) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return org.springframework.http.ResponseEntity.status(302).header("Location", "/patient/login").build();
        }

        String export = patientService.exportPatientHistory(patientId);
        byte[] content = export.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=patient-history-" + patientId + ".txt")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(content);
    }

    // ---------- LOGOUT ----------
    @GetMapping("/logout")
    public String patientLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
