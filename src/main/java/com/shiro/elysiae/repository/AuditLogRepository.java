package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.audit.AuditLog;
import com.shiro.elysiae.model.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
@Repository

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:userId IS NULL OR a.userId = :userId)
        AND   (:action IS NULL OR a.action = :action)
        AND   (:from   IS NULL OR a.performedAt >= :from)
        AND   (:to     IS NULL OR a.performedAt <= :to)
        ORDER BY a.performedAt DESC
        """)
    Page<AuditLog> findLogs(
            @Param("userId") Long userId,
            @Param("action") AuditAction action,
            @Param("from") LocalDateTime from,
            @Param("to")     LocalDateTime to,
            Pageable pageable
    );
}
