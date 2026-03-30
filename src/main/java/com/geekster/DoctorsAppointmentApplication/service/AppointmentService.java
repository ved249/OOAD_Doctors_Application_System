package com.geekster.DoctorsAppointmentApplication.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.AppointmentKey;
import com.geekster.DoctorsAppointmentApplication.model.AppointmentStatus;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.repository.IAppointmentRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IDoctorRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IPatientRepo;

@Service
public class AppointmentService {

    @Autowired
    IAppointmentRepo appointmentRepo;

    @Autowired
    IPatientRepo patientRepo;

    @Autowired
    IDoctorRepo doctorRepo;

    public void bookAppointment(Appointment appointment) {

        Optional<Appointment> myAppointment = appointmentRepo.findById(appointment.getId());
        if(myAppointment.isEmpty()){
            appointmentRepo.save(appointment);
        }else{
            throw new IllegalStateException("Appointment with id already present");
        }

    }

    public void cancelAppointment(AppointmentKey key) {
        appointmentRepo.deleteById(key);
    }

    /**
     * Book appointment from MVC form
     * @param patientId patient ID
     * @param doctorId doctor ID
     * @param appointmentDateTime appointment date-time string (format: yyyy-MM-dd'T'HH:mm)
     */
    @Transactional
    public void bookAppointmentMvc(Long patientId, Long doctorId, String appointmentDateTime) {
        // Fetch patient and doctor
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Parse the appointment date-time
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime appointmentTime = LocalDateTime.parse(appointmentDateTime, formatter);

        // Create AppointmentKey with appointment_id and time
        long appointmentId = System.currentTimeMillis() / 1000; // Simple ID generation
        AppointmentKey appointmentKey = new AppointmentKey(appointmentId, appointmentTime);

        // Check if appointment already exists for this time
        Optional<Appointment> existingAppointment = appointmentRepo.findById(appointmentKey);
        if (existingAppointment.isPresent()) {
            throw new RuntimeException("This appointment slot is already booked");
        }

        // Create and save the appointment with PENDING status
        Appointment appointment = new Appointment(appointmentKey, doctor, patient, AppointmentStatus.PENDING);
        appointmentRepo.save(appointment);
    }

    /**
     * Get all appointments for a patient
     */
    public List<Appointment> getPatientAppointments(Long patientId) {
        if (!patientRepo.existsById(patientId)) {
            throw new RuntimeException("Patient not found");
        }
        return appointmentRepo.findByPatientId(patientId);
    }

    public void saveAppointmentNotes(AppointmentKey appointmentKey, String diagnosis, String prescription, String doctorNotes) {
        Appointment appointment = appointmentRepo.findById(appointmentKey)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setDiagnosis(diagnosis);
        appointment.setPrescription(prescription);
        appointment.setDoctorNotes(doctorNotes);
        appointmentRepo.save(appointment);
    }

    public List<Appointment> getDoctorAppointments(Long doctorId) {
        Doctor doctor = doctorRepo.findByDoctorId(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor not found");
        }
        return doctor.getAppointments();
    }

    public String buildPatientHistoryExport(Long patientId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<Appointment> appointments = appointmentRepo.findByPatientId(patientId);

        StringBuilder builder = new StringBuilder();
        builder.append("Patient Record\n");
        builder.append("Name: ").append(patient.getPatientFirstName()).append(" ")
                .append(patient.getPatientLastName()).append("\n");
        builder.append("Email: ").append(patient.getPatientEmail()).append("\n");
        builder.append("Contact: ").append(patient.getPatientContact()).append("\n\n");
        builder.append("Appointments:\n");

        for (Appointment appointment : appointments) {
            builder.append("- Appointment: ")
                    .append(appointment.getId().getTime()).append("\n");
            builder.append("  Doctor: ")
                    .append(appointment.getDoctor().getDoctorName()).append(" (")
                    .append(appointment.getDoctor().getSpecialization()).append(")\n");
            builder.append("  Status: ").append(appointment.getStatus()).append("\n");
            builder.append("  Diagnosis: ")
                    .append(appointment.getDiagnosis() == null ? "N/A" : appointment.getDiagnosis()).append("\n");
            builder.append("  Prescription: ")
                    .append(appointment.getPrescription() == null ? "N/A" : appointment.getPrescription()).append("\n");
            builder.append("  Notes: ")
                    .append(appointment.getDoctorNotes() == null ? "N/A" : appointment.getDoctorNotes()).append("\n\n");
        }

        return builder.toString();
    }

    /**
     * Approve an appointment (Doctor action)
     */
    @Transactional
    public void approveAppointment(AppointmentKey appointmentKey) {
        Appointment appointment = appointmentRepo.findById(appointmentKey)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException("Only pending appointments can be approved");
        }
        
        appointment.setStatus(AppointmentStatus.APPROVED);
        appointmentRepo.save(appointment);
    }

    /**
     * Reject an appointment (Doctor action)
     */
    @Transactional
    public void rejectAppointment(AppointmentKey appointmentKey) {
        Appointment appointment = appointmentRepo.findById(appointmentKey)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException("Only pending appointments can be rejected");
        }
        
        appointment.setStatus(AppointmentStatus.REJECTED);
        appointmentRepo.save(appointment);
    }

    /**
     * Get appointment by ID
     */
    public Appointment getAppointmentById(AppointmentKey key) {
        return appointmentRepo.findById(key)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    /**
     * Reschedule an appointment
     */
    @Transactional
    public void rescheduleAppointment(AppointmentKey oldKey, String newDateTime) {
        Appointment oldAppointment = appointmentRepo.findById(oldKey)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Parse new datetime
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime newTime = LocalDateTime.parse(newDateTime, formatter);

        // Create new key
        long newAppointmentId = System.currentTimeMillis() / 1000;
        AppointmentKey newKey = new AppointmentKey(newAppointmentId, newTime);

        // Check if new slot is available
        if (appointmentRepo.findById(newKey).isPresent()) {
            throw new RuntimeException("This appointment slot is already booked");
        }

        // Create new appointment and delete old one
        Appointment newAppointment = new Appointment(newKey, oldAppointment.getDoctor(), 
                oldAppointment.getPatient(), oldAppointment.getStatus());
        appointmentRepo.save(newAppointment);
        appointmentRepo.deleteById(oldKey);
    }

    /**
     * Get upcoming appointments (after current time)
     */
    public List<Appointment> getUpcomingAppointments(Long patientId) {
        return appointmentRepo.findUpcomingAppointmentsByPatientId(patientId);
    }

    /**
     * Get past appointments (before current time)
     */
    public List<Appointment> getPastAppointments(Long patientId) {
        return appointmentRepo.findPastAppointmentsByPatientId(patientId);
    }

    /**
     * Filter appointments by date range
     */
    public List<Appointment> getAppointmentsByDateRange(Long patientId, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepo.findAppointmentsByDateRange(patientId, startDate, endDate);
    }
}
