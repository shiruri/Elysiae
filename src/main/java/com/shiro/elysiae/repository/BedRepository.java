package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.enums.BedStatus;
import com.shiro.elysiae.model.wardsbedsadmission.Bed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {
    @Query("""
        SELECT b
        FROM Bed b
        WHERE b.ward.id = :wardId
          AND b.deletedAt IS NULL
          AND (:status IS NULL OR b.status = :status)
    """)
    Page<Bed> findByWardAndStatus(
            Long wardId,
            BedStatus status,
            Pageable pageable
    );

    @Query("""
    SELECT b
    FROM Admission a
    JOIN a.bed b
    WHERE a.patient.id = :patientId
    AND a.status = 'ADMITTED'
""")
    Optional<Bed> findCurrentBedByPatientId(Long patientId);
}
