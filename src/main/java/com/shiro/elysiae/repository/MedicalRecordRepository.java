package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.ehrnprescriptionsnvitals.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    @Query(
            value = """
            SELECT mr FROM MedicalRecord mr
            JOIN FETCH mr.doctor d
            LEFT JOIN FETCH mr.prescriptions p
            WHERE mr.patient.id = :patientId
            ORDER BY mr.recordDate DESC
            """,
            countQuery = """
            SELECT COUNT(mr) FROM MedicalRecord mr
            WHERE mr.patient.id = :patientId
            """
    )
    Page<MedicalRecord> findByPatientId(
            @Param("patientId") Long patientId,
            Pageable pageable
    );
}
