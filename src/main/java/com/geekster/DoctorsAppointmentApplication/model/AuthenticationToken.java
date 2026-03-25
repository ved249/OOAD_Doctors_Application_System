package com.geekster.DoctorsAppointmentApplication.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
public class AuthenticationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;
    private String token;
    private LocalDate tokenCreationDate;

    @OneToOne
    @JoinColumn(nullable = false , name = "fk_patient_ID")
    private Patient patient;

    public AuthenticationToken() {
    }

    public AuthenticationToken(Patient patient) {
        this.patient = patient;
        this.tokenCreationDate = LocalDate.now();
        this.token = UUID.randomUUID().toString();
    }

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDate getTokenCreationDate() {
        return tokenCreationDate;
    }

    public void setTokenCreationDate(LocalDate tokenCreationDate) {
        this.tokenCreationDate = tokenCreationDate;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}