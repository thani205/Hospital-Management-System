package com.example.hospital_manager.dto;

public record LoginRequest(
    String username,
    String password
) {
}