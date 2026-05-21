package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.pharmacy.DispenseLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DispenseLogRepository extends JpaRepository<DispenseLog, Long> {

    Page<DispenseLog> findByPrescriptionId(
            Long prescriptionId,
            Pageable pageable
    );
}
