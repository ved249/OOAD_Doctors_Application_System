package com.geekster.DoctorsAppointmentApplication.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

@Entity
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    scope = Appointment.class,
    property = "id"
)
public class Appointment {

    @Id
    @EmbeddedId
    private AppointmentKey id;

    @ManyToOne
    @JoinColumn(name = "fk_doctor_doc_id")
    private Doctor doctor;

    @OneToOne
    private Patient patient;

    public Appointment() {
    }

    public Appointment(AppointmentKey id, Doctor doctor, Patient patient) {
        this.id = id;
        this.doctor = doctor;
        this.patient = patient;
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
}