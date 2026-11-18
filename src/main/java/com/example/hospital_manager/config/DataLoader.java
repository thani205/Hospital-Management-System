package com.example.hospital_manager.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.hospital_manager.model.AppUser;
import com.example.hospital_manager.model.Patient;
import com.example.hospital_manager.model.Role;
import com.example.hospital_manager.repository.PatientRepository;
import com.example.hospital_manager.repository.UserRepository;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            PatientRepository patientRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Create default ADMIN account
            if (userRepository.findByUsername("admin").isEmpty()) {

                AppUser admin = new AppUser();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("Hospital Admin");
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
            }

            // Create default DOCTOR account
            if (userRepository.findByUsername("doctor").isEmpty()) {

                AppUser doctor = new AppUser();
                doctor.setUsername("doctor");
                doctor.setPassword(passwordEncoder.encode("doctor123"));
                doctor.setFullName("Hospital Doctor");
                doctor.setRole(Role.DOCTOR);

                userRepository.save(doctor);
            }

            // Create default PATIENT account
            AppUser patientUser = userRepository
                    .findByUsername("patient")
                    .orElseGet(() -> {

                        AppUser patient = new AppUser();
                        patient.setUsername("patient");
                        patient.setPassword(
                                passwordEncoder.encode("patient123")
                        );
                        patient.setFullName("Sample Patient");
                        patient.setRole(Role.PATIENT);

                        return userRepository.save(patient);
                    });

            // Create patient profile if it does not exist
            if (patientRepository
                    .findByUserUsername("patient")
                    .isEmpty()) {

                Patient patient = new Patient();
                patient.setUser(patientUser);
                patient.setName("Sample Patient");
                patient.setDateOfBirth(
                        LocalDate.of(2000, 1, 15)
                );
                patient.setGender("Male");
                patient.setPhone("0771234567");
                patient.setEmail("patient@example.com");
                patient.setAddress("Colombo, Sri Lanka");

                patientRepository.save(patient);
            }
        };
    }
}