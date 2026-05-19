package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.doctorsndepartment.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @Query("""
        SELECT d
        FROM Department d
        WHERE d.deletedAt IS NULL
        AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(d.floor) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<Department> searchDepartments(String keyword, Pageable pageable);

    @Query("SELECT d FROM Department d WHERE d.deletedAt IS NULL")
    Page<Department> findAllActive(Pageable pageable);
}
