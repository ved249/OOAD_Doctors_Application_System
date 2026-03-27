package com.geekster.DoctorsAppointmentApplication.dto;

import com.geekster.DoctorsAppointmentApplication.model.Specialization;

public class DoctorSignUpInput {

    private String doctorName;
    private String doctorEmail;
    private String doctorPassword;
    private Specialization specialization;

    public DoctorSignUpInput() {
    }

    public DoctorSignUpInput(String doctorName, String doctorEmail, String doctorPassword, Specialization specialization) {
        this.doctorName = doctorName;
        this.doctorEmail = doctorEmail;
        this.doctorPassword = doctorPassword;
        this.specialization = specialization;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public String getDoctorPassword() {
        return doctorPassword;
    }

    public void setDoctorPassword(String doctorPassword) {
        this.doctorPassword = doctorPassword;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
    }
}