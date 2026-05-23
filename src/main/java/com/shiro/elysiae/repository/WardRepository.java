package com.shiro.elysiae.repository;

import com.shiro.elysiae.dto.response.wardsandbeds.WardsSummary;
import com.shiro.elysiae.model.enums.WardType;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.model.wardsbedsadmission.Ward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {

    @Query("""
    SELECT new com.shiro.elysiae.dto.response.wardsandbeds.WardsSummary(
        w.id,
        w.name,
        w.type,
        w.floor,
        COUNT(b),
        COALESCE(SUM(CASE WHEN b.status = 'AVAILABLE' THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN b.status = 'OCCUPIED' THEN 1 ELSE 0 END), 0)
    )
    FROM Ward w
    LEFT JOIN w.beds b
    WHERE w.deletedAt IS NULL
      AND (:name IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:type IS NULL OR w.type = :type)
      AND (:floor IS NULL OR LOWER(w.floor) LIKE LOWER(CONCAT('%', :floor, '%')))
    GROUP BY w.id, w.name, w.type, w.floor
""")
    Page<WardsSummary> searchWards(
            String name,
            WardType type,
            String floor,
            Pageable pageable
    );

    Patient findByPatientId(@Param("patient_id")long patientId);
}
