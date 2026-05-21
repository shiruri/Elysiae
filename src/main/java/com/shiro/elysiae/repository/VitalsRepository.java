package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.ehrnprescriptionsnvitals.Vitals;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VitalsRepository extends JpaRepository<Vitals, Long> {

    @Query(
            value = """
            SELECT v FROM Vitals v
            JOIN FETCH v.patient p
            JOIN FETCH v.recordedBy u
            WHERE v.patient.id = :patientId
            ORDER BY v.recordedAt DESC
            """,
            countQuery = """
            SELECT COUNT(v) FROM Vitals v
            WHERE v.patient.id = :patientId
            """
    )
    Page<Vitals> findByPatientId(@Param("patientId") Long patientId, Pageable pageable);
}