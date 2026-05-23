package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.deletedAt IS NULL")
    long countActive();
    @Query("SELECT d FROM Doctor d WHERE d.user.id = :userId AND d.deletedAt IS NULL")
    Optional<Doctor> findByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT d
        FROM Doctor d
        LEFT JOIN d.department dep
        WHERE d.deletedAt IS NULL
        AND (:doctorId IS NULL OR d.id = :doctorId)
        AND (:departmentName IS NULL OR LOWER(dep.name) LIKE LOWER(CONCAT('%', :departmentName, '%')))
        AND (:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%')))
    """)
    Page<Doctor> searchDoctors(
            @Param("doctorId") Long doctorId,
            @Param("departmentName") String departmentName,
            @Param("specialization") String specialization,
            Pageable pageable
    );
    boolean existsByLicenseNumberAndDeletedAtIsNull(String licenseNumber);
    @Query("""
    SELECT d
    FROM Doctor d
    WHERE d.department.id = :departmentId
    AND d.deletedAt IS NULL
""")
    List<Doctor> searchByDepartmentId(@Param("departmentId") Long departmentId);
}
