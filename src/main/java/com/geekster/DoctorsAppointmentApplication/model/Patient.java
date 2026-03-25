package com.geekster.DoctorsAppointmentApplication.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;


@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "patientId")
public class Patient {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long patientId;
        private String patientFirstName;
        private String patientLastName;
        @Column(nullable = false, unique = true)
        private String patientEmail;
        @Column(nullable = false)
        private String patientPassword;
        private String patientContact;

        @OneToOne(mappedBy = "patient")
        private Appointment appointment;

        public Patient() {
        }

        public Patient(String patientFirstName , String patientLastName ,String patientEmail , String patientPassword,String patientContact ){
            this.patientFirstName = patientFirstName;
            this.patientLastName = patientLastName;
            this.patientEmail = patientEmail;
            this.patientPassword = patientPassword;
            this.patientContact = patientContact;
        }

        public Long getPatientId() {
            return patientId;
        }

        public void setPatientId(Long patientId) {
            this.patientId = patientId;
        }

        public String getPatientFirstName() {
            return patientFirstName;
        }

        public void setPatientFirstName(String patientFirstName) {
            this.patientFirstName = patientFirstName;
        }

        public String getPatientLastName() {
            return patientLastName;
        }

        public void setPatientLastName(String patientLastName) {
            this.patientLastName = patientLastName;
        }

        public String getPatientEmail() {
            return patientEmail;
        }

        public void setPatientEmail(String patientEmail) {
            this.patientEmail = patientEmail;
        }

        public String getPatientPassword() {
            return patientPassword;
        }

        public void setPatientPassword(String patientPassword) {
            this.patientPassword = patientPassword;
        }

        public String getPatientContact() {
            return patientContact;
        }

        public void setPatientContact(String patientContact) {
            this.patientContact = patientContact;
        }

        public Appointment getAppointment() {
            return appointment;
        }

        public void setAppointment(Appointment appointment) {
            this.appointment = appointment;
        }
}

