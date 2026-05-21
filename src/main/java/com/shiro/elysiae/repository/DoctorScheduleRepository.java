package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.doctorsndepartment.DoctorSchedule;
import com.shiro.elysiae.model.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule,Long> {

    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId AND ds.dayOfWeek = :dayOfWeek AND ds.doctor.deletedAt IS NULL")
    DoctorSchedule findByDoctorIdAndDayOfWeek(@Param("doctorId") long doctorId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT ds FROM DoctorSchedule ds JOIN FETCH ds.doctor d WHERE d.id = :doctorId AND d.deletedAt IS NULL")
    List<DoctorSchedule> findByDoctorId(@Param("doctorId") Long doctorId);

    @Query("""
        SELECT ds FROM DoctorSchedule ds
        WHERE ds.doctor.id = :doctorId
        AND ds.doctor.deletedAt IS NULL
        AND ds.dayOfWeek = :dayOfWeek
        AND ds.startTime < :endTime
        AND ds.endTime > :startTime
        """)
    List<DoctorSchedule> findOverlapping(
            @Param("doctorId") Long doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
