package com.geekster.DoctorsAppointmentApplication.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    scope = Appointment.class,
    property = "id"
)
public class Appointment {

    @EmbeddedId
    private AppointmentKey id;

    @ManyToOne
    @JoinColumn(name = "fk_doctor_doc_id")
    private Doctor doctor;

    @OneToOne
    private Patient patient;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private String diagnosis;
    private String prescription;
    private String doctorNotes;

    public Appointment() {
        this.status = AppointmentStatus.PENDING; // Default status
    }

    public Appointment(AppointmentKey id, Doctor doctor, Patient patient) {
        this.id = id;
        this.doctor = doctor;
        this.patient = patient;
        this.status = AppointmentStatus.PENDING; // Default status
    }

    public Appointment(AppointmentKey id, Doctor doctor, Patient patient, AppointmentStatus status) {
        this.id = id;
        this.doctor = doctor;
        this.patient = patient;
        this.status = status;
    }

    public Appointment(AppointmentKey id, Doctor doctor, Patient patient, AppointmentStatus status,
                       String diagnosis, String prescription, String doctorNotes) {
        this.id = id;
        this.doctor = doctor;
        this.patient = patient;
        this.status = status;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.doctorNotes = doctorNotes;
    }

    public AppointmentKey getId() {
        return id;
    }

    public void setId(AppointmentKey id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }
}