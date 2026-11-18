package com.example.hospital_manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hospital_manager.model.MedicalRecord;
import com.example.hospital_manager.model.Patient;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatient(Patient patient);

    boolean existsByPatient(Patient patient);

}