package com.example.hospital_manager.dto;

import com.example.hospital_manager.model.AppointmentStatus;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(

    @NotNull(message = "Appointment status is required")
    AppointmentStatus status

) {
}