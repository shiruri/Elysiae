package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.ehrnprescriptionsnvitals.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    @Query(
            value = """
        SELECT DISTINCT r FROM MedicalRecord r
        JOIN FETCH r.patient p
        JOIN FETCH r.doctor d
        LEFT JOIN FETCH r.prescriptions pr
        WHERE r.patient.id = :patientId
        ORDER BY r.recordDate DESC
        """,
            countQuery = """
        SELECT COUNT(r) FROM MedicalRecord r
        WHERE r.patient.id = :patientId
        """
    )
    Page<MedicalRecord> findByPatientId(@Param("patientId") Long patientId, Pageable pageable);
    @Query("""
        SELECT r FROM MedicalRecord r
        JOIN FETCH r.patient p
        JOIN FETCH r.doctor d
        LEFT JOIN FETCH r.prescriptions pr
        WHERE r.id = :id
        """)
    Optional<MedicalRecord> findByIdWithPrescriptions(@Param("id") Long id);
}
