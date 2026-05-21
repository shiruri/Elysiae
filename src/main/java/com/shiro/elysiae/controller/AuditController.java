package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.audit.AuditRequest;
import com.shiro.elysiae.dto.response.audit.AuditResponse;
import com.shiro.elysiae.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit-log")
public class AuditController {
    private final AuditService auditService;

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditResponse>> getAudit(AuditRequest auditRequest, Pageable pageable) {
        return ResponseEntity.ok().body(auditService.findAudits(auditRequest,pageable));
    }
}
