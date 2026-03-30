package com.geekster.DoctorsAppointmentApplication.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDateTime;

@Embeddable
public class AppointmentKey {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppointmentKey that = (AppointmentKey) o;
        return appointmentId != null && appointmentId.equals(that.appointmentId)
                && time != null && time.equals(that.time);
    }

    @Override
    public int hashCode() {
        int result = appointmentId != null ? appointmentId.hashCode() : 0;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        return result;
    }
}