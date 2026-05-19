package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.patient.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository

public interface PatientRepository extends JpaRepository<Patient,Long> {
    java.util.Optional<Patient> findByUser_Id(Long userId);

    @Query("""
            SELECT p FROM Patient p
            WHERE (:keyword IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                    OR LOWER(p.lastName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
                                    OR LOWER(p.email)     LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND   (:gender    IS NULL OR p.gender    = :gender)
            AND   (:bloodType IS NULL OR p.bloodType = :bloodType)
            AND   (:ageFrom   IS NULL OR YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) >= :ageFrom)
            AND   (:ageTo     IS NULL OR YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) <= :ageTo)
            """)
    Page<Patient> searchPatients(
            @Param("keyword")   String keyword,
            @Param("gender")    String gender,
            @Param("bloodType") String bloodType,
            @Param("ageFrom")   Integer ageFrom,
            @Param("ageTo")     Integer ageTo,
            Pageable pageable
    );
}
