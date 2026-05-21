package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.enums.LabPriority;
import com.shiro.elysiae.model.enums.LabRequestStatus;
import com.shiro.elysiae.model.laborotory.LabRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LabRequestRepository extends JpaRepository<LabRequest,Long> {

    @Query(
            value = """
            SELECT r FROM LabRequest r
            JOIN FETCH r.patient p
            JOIN FETCH r.doctor d
            WHERE (:patientId = 0 OR r.patient.id = :patientId)
            AND   (:doctorId  = 0 OR r.doctor.id  = :doctorId)
            AND   (:status   IS NULL OR r.status   = :status)
            AND   (:priority IS NULL OR r.priority = :priority)
            """,
            countQuery = """
            SELECT COUNT(r) FROM LabRequest r
            WHERE (:patientId = 0 OR r.patient.id = :patientId)
            AND   (:doctorId  = 0 OR r.doctor.id  = :doctorId)
            AND   (:status   IS NULL OR r.status   = :status)
            AND   (:priority IS NULL OR r.priority = :priority)
            """
    )
    Page<LabRequest> search(
            @Param("patientId") Long patientId,
            @Param("doctorId")  Long doctorId,
            @Param("status") LabRequestStatus status,
            @Param("priority") LabPriority priority,
            Pageable pageable
    );
}

