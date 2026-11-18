package com.example.hospital_manager.dto;

import java.time.LocalDateTime;

import com.example.hospital_manager.model.AppointmentStatus;

public record AppointmentResponse(
    Long id,
    Long patientId,
    String patientName,
    Long doctorId,
    String doctorName,
    LocalDateTime appointmentDateTime,
    AppointmentStatus status
) {
}