package com.geekster.DoctorsAppointmentApplication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.model.Specialization;
import com.geekster.DoctorsAppointmentApplication.service.AdminService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminMvcController {

    @Autowired
    AdminService adminService;

    // ========== AUTHENTICATION ==========

    /**
     * Admin Login Page
     */
    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin-login";
    }

    /**
     * Admin Login Submit
     */
    @PostMapping("/login")
    public String adminLoginSubmit(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        try {
            var admin = adminService.authenticateAdmin(email, password);
            if (admin != null) {
                session.setAttribute("adminId", admin.getAdminId());
                session.setAttribute("adminName", admin.getAdminName());
                session.setAttribute("adminEmail", admin.getAdminEmail());
                return "redirect:/admin/dashboard";
            }
            model.addAttribute("error", "Invalid email or password. Please try again.");
            return "admin-login";
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "admin-login";
        }
    }

    /**
     * Admin Dashboard
     */
    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/admin/login";
        }

        try {
            long doctorCount = adminService.getAllDoctors().size();
            long patientCount = adminService.getAllPatients().size();
            long appointmentCount = adminService.getAllAppointments().size();

            model.addAttribute("doctorCount", doctorCount);
            model.addAttribute("patientCount", patientCount);
            model.addAttribute("appointmentCount", appointmentCount);
            model.addAttribute("adminName", session.getAttribute("adminName"));
            return "admin-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "admin-dashboard";
        }
    }

    /**
     * Admin Logout
     */
    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // ========== DOCTOR MANAGEMENT ==========

    /**
     * View All Doctors
     */
    @GetMapping("/doctors")
    public String viewAllDoctors(HttpSession session, Model model) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/admin/login";
        }

        try {
            List<Doctor> doctors = adminService.getAllDoctors();
            model.addAttribute("doctors", doctors);
            model.addAttribute("specializations", Specialization.values());
            return "admin-view-doctors";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading doctors: " + e.getMessage());
            return "admin-view-doctors";
        }
    }

    /**
     * Add Doctor Page
     */
    @GetMapping("/add-doctor")
    public String addDoctorPage(HttpSession session, Model model) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("specializations", Specialization.values());
        return "admin-add-doctor";
    }

    /**
     * Add Doctor Submit
     */
    @PostMapping("/add-doctor")
    public String addDoctorSubmit(
            @RequestParam String doctorName,
            @RequestParam String doctorEmail,
            @RequestParam String doctorPassword,
            @RequestParam Specialization specialization,
            HttpSession session,
            Model model
    ) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/admin/login";
        }

        try {
            Doctor doctor = new Doctor(doctorName, doctorEmail, doctorPassword, specialization);
            adminService.addDoctor(doctor);
            model.addAttribute("message", "Doctor added successfully!");
            model.addAttribute("doctors", adminService.getAllDoctors());
            model.addAttribute("specializations", Specialization.values());
            return "admin-view-doctors";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add doctor: " + e.getMessage());
            model.addAttribute("specializations", Specialization.values());
            return "admin-add-doctor";
        }
    }

    /**
     * Edit Doctor Page
     */
    @GetMapping("/edit-doctor")
    public String editDoctorPage(
            @RequestParam Long doctorId,
            HttpSession session,
            Model model
    ) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/admin/login";
        }

        try {
            Doctor doctor = adminService.getDoctorById(doctorId);
            if (doctor == null) {
                model.addAttribute("error", "Doctor not found");
                return "admin-view-doctors";
            }
            model.addAttribute("doctor", doctor);
            model.addAttribute("specializations", Specialization.values());
            return "admin-edit-doctor";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading doctor: " + e.getMessage());
            return "admin-view-doctors";
        }
    }

    /**
     * Update Doctor Submit
     */
    @PostMapping("/edit-doctor")
    public String editDoctorSubmit(
            @RequestParam Long doctorId,
            @RequestParam String doctorName,
            @RequestParam Specialization specialization,
            HttpSession session,
            Model model
    ) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/admin/login";
        }

        try {
            Doctor doctor = adminService.getDoctorById(doctorId);
            if (doctor == null) {
                model.addAttribute("error", "Doctor not found");
                return "admin-view-doctors";
            }
            doctor.setDoctorName(doctorName);
            doctor.setSpecialization(specialization);
            adminService.updateDoctor(doctorId, doctor);
            model.addAttribute("message", "Doctor updated successfully!");
            model.addAttribute("doctors", adminService.getAllDoctors());
            model.addAttribute("specializations", Specialization.values());
            return "admin-view-doctors";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update doctor: " + e.getMessage());
            return "redirect:/admin/edit-doctor?doctorId=" + doctorId;
        }
    }

    /**
     * Delete Doctor
     */
    @PostMapping("/delete-doctor")
    public String deleteDoctor(
            @RequestParam Long doctorId,
            HttpSession session,
            Model model
    ) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/admin/login";
        }

        try {
            adminService.deleteDoctor(doctorId);
            model.addAttribute("message", "Doctor deleted successfully!");
            model.addAttribute("doctors", adminService.getAllDoctors());
            model.addAttribute("specializations", Specialization.values());
            return "admin-view-doctors";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete doctor: " + e.getMessage());
            return "redirect:/admin/doctors";
        }
    }

    // ========== APPOINTMENT MONITORING ==========

    /**
     * View All Appointments
     */
    @GetMapping("/appointments")
    public String viewAllAppointments(HttpSession session, Model model) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/admin/login";
        }

        try {
            List<Appointment> appointments = adminService.getAllAppointments();
            model.addAttribute("appointments", appointments);
            return "admin-view-appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading appointments: " + e.getMessage());
            return "admin-view-appointments";
        }
    }

    // ========== PATIENT MANAGEMENT ==========

    /**
     * View All Patients
     */
    @GetMapping("/patients")
    public String viewAllPatients(HttpSession session, Model model) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/admin/login";
        }

        try {
            List<Patient> patients = adminService.getAllPatients();
            model.addAttribute("patients", patients);
            return "admin-view-patients";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading patients: " + e.getMessage());
            return "admin-view-patients";
        }
    }

    /**
     * Delete Patient
     */
    @PostMapping("/delete-patient")
    public String deletePatient(
            @RequestParam Long patientId,
            HttpSession session,
            Model model
    ) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/admin/login";
        }

        try {
            adminService.deletePatient(patientId);
            model.addAttribute("message", "Patient deleted successfully!");
            model.addAttribute("patients", adminService.getAllPatients());
            return "admin-view-patients";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete patient: " + e.getMessage());
            return "redirect:/admin/patients";
        }
    }
}
