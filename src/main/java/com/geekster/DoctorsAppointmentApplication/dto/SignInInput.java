package com.geekster.DoctorsAppointmentApplication.dto;

public class SignInInput {

    private String patientEmail;
    private String patientPassword;

    public SignInInput() {
    }

    public SignInInput(String patientEmail, String patientPassword) {
        this.patientEmail = patientEmail;
        this.patientPassword = patientPassword;
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
}