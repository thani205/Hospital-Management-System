package com.example.hospital_manager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.hospital_manager.dto.RegisterRequest;
import com.example.hospital_manager.dto.UserResponse;
import com.example.hospital_manager.model.Role;
import com.example.hospital_manager.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class StaffController {

    private final AuthService authService;

    public StaffController(AuthService authService) {
        this.authService = authService;
    }

    // Get all users - ADMIN only
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return authService.getAllUsers();
    }

    // Get all doctors - ADMIN and PATIENT
    @GetMapping("/doctors")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public List<UserResponse> getAllDoctors() {
        return authService.getAllDoctors();
    }

    // Add a new doctor - ADMIN only
    @PostMapping("/admin/doctors")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse addDoctor(
            @Valid @RequestBody RegisterRequest request) {

        if (request.role() != Role.DOCTOR) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role must be DOCTOR"
            );
        }

        return authService.register(request);
    }
}