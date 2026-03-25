package com.geekster.DoctorsAppointmentApplication.service;

import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.AppointmentKey;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.repository.IAppointmentRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IDoctorRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IPatientRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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

        // Create and save the appointment
        Appointment appointment = new Appointment(appointmentKey, doctor, patient);
        appointmentRepo.save(appointment);
    }
}
