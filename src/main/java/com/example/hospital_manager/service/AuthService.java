package com.example.hospital_manager.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hospital_manager.dto.AuthResponse;
import com.example.hospital_manager.dto.LoginRequest;
import com.example.hospital_manager.dto.RegisterRequest;
import com.example.hospital_manager.dto.UserResponse;
import com.example.hospital_manager.exception.ApiException;
import com.example.hospital_manager.model.AppUser;
import com.example.hospital_manager.model.Patient;
import com.example.hospital_manager.model.Role;
import com.example.hospital_manager.repository.PatientRepository;
import com.example.hospital_manager.repository.UserRepository;
import com.example.hospital_manager.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PatientRepository patientRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        if (request.role() == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "User role is required"
            );
        }

        // Nurse and Receptionist registration requires ADMIN
        if (request.role() == Role.NURSE
                || request.role() == Role.RECEPTIONIST) {

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication.getAuthorities().stream()
                            .noneMatch(authority ->
                                    authority.getAuthority().equals("ROLE_ADMIN"))) {

                throw new ApiException(
                        authentication == null
                                || !authentication.isAuthenticated()
                                || authentication.getAuthorities().stream()
                                        .anyMatch(authority ->
                                                authority.getAuthority().equals("ROLE_ANONYMOUS"))
                                ? HttpStatus.UNAUTHORIZED
                                : HttpStatus.FORBIDDEN,
                        "Only an authenticated ADMIN can register staff"
                );
            }
        }

        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        // Validate patient details
        if (request.role() == Role.PATIENT) {

            if (request.dateOfBirth() == null
                    || !request.dateOfBirth().isBefore(LocalDate.now())
                    || request.gender() == null
                    || request.gender().isBlank()
                    || request.phone() == null
                    || request.phone().isBlank()
                    || request.address() == null
                    || request.address().isBlank()) {

                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Complete all required patient details"
                );
            }
        }

        AppUser user = new AppUser();

        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.name());
        user.setRole(request.role());

        AppUser savedUser = userRepository.save(user);

        // Create patient profile when registering a patient
        if (request.role() == Role.PATIENT) {

            Patient patient = new Patient();

            patient.setUser(savedUser);
            patient.setName(request.name());
            patient.setDateOfBirth(request.dateOfBirth());
            patient.setGender(request.gender());
            patient.setPhone(request.phone());
            patient.setEmail(request.email());
            patient.setAddress(request.address());

            patientRepository.save(patient);
        }

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getFullName(),
                savedUser.getRole()
        );
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid username or password"
                ));

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationTime(),
                user.getRole().name()
        );
    }

    public UserResponse getCurrentUser(String username) {

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole()
        );
    }

    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getRole()
                ))
                .toList();
    }

    public List<UserResponse> getAllDoctors() {

        return userRepository.findByRole(Role.DOCTOR)
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getRole()
                ))
                .toList();
    }
}