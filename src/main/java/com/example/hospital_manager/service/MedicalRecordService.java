package com.example.hospital_manager.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hospital_manager.dto.MedicalRecordRequest;
import com.example.hospital_manager.dto.MedicalRecordResponse;
import com.example.hospital_manager.exception.ApiException;
import com.example.hospital_manager.model.AppUser;
import com.example.hospital_manager.model.MedicalRecord;
import com.example.hospital_manager.model.Patient;
import com.example.hospital_manager.model.Role;
import com.example.hospital_manager.repository.MedicalRecordRepository;
import com.example.hospital_manager.repository.PatientRepository;
import com.example.hospital_manager.repository.UserRepository;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public MedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            PatientRepository patientRepository,
            UserRepository userRepository) {

        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MedicalRecordResponse createMedicalRecord(
            String doctorUsername,
            MedicalRecordRequest request) {

        AppUser doctor = userRepository.findByUsername(doctorUsername)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Doctor not found"
                ));

        if (doctor.getRole() != Role.DOCTOR) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Only doctors can create medical records"
            );
        }

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Patient not found"
                ));

        MedicalRecord record = new MedicalRecord();

        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setDiagnosis(request.diagnosis());
        record.setPrescription(request.prescription());

        MedicalRecord savedRecord =
                medicalRecordRepository.save(record);

        return mapToResponse(savedRecord);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getPatientRecords(Long patientId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Patient not found"
                ));

        return medicalRecordRepository.findByPatient(patient)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private MedicalRecordResponse mapToResponse(
            MedicalRecord record) {

        return new MedicalRecordResponse(
                record.getId(),
                record.getPatient().getId(),
                record.getPatient().getName(),
                record.getDoctor().getId(),
                record.getDoctor().getFullName(),
                record.getDiagnosis(),
                record.getPrescription(),
                record.getCreatedAt()
        );
    }
}