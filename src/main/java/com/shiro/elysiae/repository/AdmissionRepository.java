package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.enums.AdmissionStatus;
import com.shiro.elysiae.model.wardsbedsadmission.Admission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, Long> {
    @Query("""
    SELECT a
    FROM Admission a
    WHERE a.patient.id = :patientId
    ORDER BY a.admittedAt DESC
""")
    List<Admission> findByPatient(Long patientId);

    @Query(
            value = """
        SELECT a FROM Admission a
        JOIN FETCH a.patient p
        JOIN FETCH a.admittingDoctor d
        WHERE a.admittingDoctor.id = :doctorId
        """,
            countQuery = """
        SELECT COUNT(a) FROM Admission a
        WHERE a.admittingDoctor.id = :doctorId
        """
    )
    Page<Admission> findByAdmittingDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);

    @Query(
            value = """
        SELECT a FROM Admission a
        JOIN FETCH a.admittingDoctor d
        WHERE a.patient.id = :patientId
        ORDER BY a.admittedAt DESC
        """,
            countQuery = """
        SELECT COUNT(a) FROM Admission a
        WHERE a.patient.id = :patientId
        """
    )
    Page<Admission> findByAdmissionFromPatientId(@Param("patientId") Long patientId, Pageable pageable);
    boolean existsByPatientIdAndStatus(Long patientId, AdmissionStatus status);
    boolean existsByAdmittingDoctorIdAndStatus(Long doctorId, AdmissionStatus status);
    Optional<Admission> findTopByPatientIdAndStatusOrderByAdmittedAtDesc(Long patientId, AdmissionStatus status);
}
