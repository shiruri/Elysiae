package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.laborotory.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult,Long> {
    Optional<LabResult> findByLabRequestId(Long labRequestId);
}
