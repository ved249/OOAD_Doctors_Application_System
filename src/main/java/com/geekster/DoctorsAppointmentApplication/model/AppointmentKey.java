package com.geekster.DoctorsAppointmentApplication.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDateTime;

@Embeddable
public class AppointmentKey {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long appointmentId;

    public LocalDateTime time;

    public AppointmentKey() {
    }

    public AppointmentKey(Long appointmentId, LocalDateTime time) {
        this.appointmentId = appointmentId;
        this.time = time;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}