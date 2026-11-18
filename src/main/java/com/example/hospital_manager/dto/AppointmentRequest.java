package com.example.hospital_manager.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record AppointmentRequest(

    @NotNull(message = "Doctor ID is required")
    Long doctorId,

    @NotNull(message = "Appointment date and time is required")
    @Future(message = "Appointment date must be in the future")
    LocalDateTime appointmentDateTime

) {
}