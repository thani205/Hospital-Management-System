package com.example.hospital_manager.dto;

import java.time.LocalDate;

import com.example.hospital_manager.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

    @NotBlank
    String name,

    @NotBlank
    String username,

    @NotBlank
    @Size(min = 6)
    String password,

    @NotNull
    Role role,

    LocalDate dateOfBirth,

    String gender,

    String phone,

    @Email
    String email,

    String address

) {}