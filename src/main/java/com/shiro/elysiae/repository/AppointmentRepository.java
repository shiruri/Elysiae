package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.appointments.Appointment;
import com.shiro.elysiae.model.enums.AppointmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment,Long> {

    @Query(
            value = """
            SELECT a FROM Appointment a
            JOIN FETCH a.doctor d
            WHERE a.patient.id = :patientId
            ORDER BY a.appointmentDateTime DESC
            """,
            countQuery = """
            SELECT COUNT(a) FROM Appointment a
            WHERE a.patient.id = :patientId
            """
    )
    Page<Appointment> findByPatientId(
            @Param("patientId") Long patientId,
            Pageable pageable
    );
    @Query("""
    SELECT a
    FROM Appointment a
    JOIN FETCH a.doctor d
    WHERE a.patient.id = :patientId
    AND a.status = 'COMPLETED'
    AND a.invoice IS NULL
    ORDER BY a.appointmentDateTime DESC
""")
    List<Appointment> findBillableAppointments(
            @Param("patientId") Long patientId
    );
    @Query("""
    SELECT a FROM Appointment a
    WHERE (:patientId = 0 OR a.patient.id = :patientId)
    AND   (:doctorId  = 0 OR a.doctor.id = :doctorId)
    AND   (:type IS NULL OR a.type = :type)
    AND   (:from IS NULL OR a.appointmentDateTime >= :from)
    AND   (:to   IS NULL OR a.appointmentDateTime <= :to)
""")
    Page<Appointment> searchAppointments(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("type") AppointmentType type,
            Pageable pageable
    );

    @Query("""
    SELECT a FROM Appointment a
    JOIN FETCH a.doctor d
    WHERE a.patient.id = :patientId
    ORDER BY a.appointmentDateTime DESC
""")
    Page<Appointment> searchAppointmentsByPatientId(
            @Param("user_id") Long userId,
            Pageable pageable
    );
    @Query("""
    SELECT a FROM Appointment a
    WHERE a.doctor.id = :doctorId
    AND a.appointmentDateTime >= :startOfDay
    AND a.appointmentDateTime < :endOfDay
""")
    List<Appointment> findDoctorAppointmentsForDay(
            @Param("doctorId") Long doctorId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
    @Query(
            value = """
        SELECT a FROM Appointment a
        JOIN FETCH a.patient p
        WHERE a.doctor.id = :doctorId
        AND a.appointmentDateTime >= :from
        """,
            countQuery = """
        SELECT COUNT(a) FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.appointmentDateTime >= :from
        """
    )
    Page<Appointment> findAppointmentsByDoctorIdFrom(
            @Param("doctorId") Long doctorId,
            @Param("from") LocalDateTime from,
            Pageable pageable
    );
}


