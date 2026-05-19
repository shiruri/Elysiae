package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.audit.AuditRequest;
import com.shiro.elysiae.dto.response.audit.AuditResponse;
import com.shiro.elysiae.model.audit.AuditLog;
import com.shiro.elysiae.repository.AuditLogRepository;
import com.shiro.elysiae.util.AuditMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditMapper auditMapper;
    @Transactional
    public void log(String action, String entity, Long entityId) {
        auditLogRepository.save(AuditLog.builder()
                .userId(extractCurrentUserId())
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .build());
    }
    private Long extractCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return Long.parseLong(auth.getName());
    }
    @Transactional(readOnly = true)
    public Page<AuditResponse> findAudits(AuditRequest auditRequest,
                                          Pageable  pageable) {
        return auditLogRepository.findLogs(auditRequest.userId(),auditRequest.action()
        ,auditRequest.from(),
                auditRequest.to(),
                pageable).map(auditMapper::toAuditResponse);
    }
}