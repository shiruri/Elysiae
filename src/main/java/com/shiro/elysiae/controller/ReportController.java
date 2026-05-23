package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.response.dashboard.BedOccupancyResponse;
import com.shiro.elysiae.dto.response.dashboard.DashboardResponse;
import com.shiro.elysiae.dto.response.dashboard.RevenueReportResponse;
import com.shiro.elysiae.service.AuditService;
import com.shiro.elysiae.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final AuditService auditService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(reportService.getDashboard());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue")
    public ResponseEntity<RevenueReportResponse> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getRevenueReport(from, to));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/beds/occupancy")
    public ResponseEntity<List<BedOccupancyResponse>> getBedOccupancy() {
        return ResponseEntity.ok(reportService.getBedOccupancy());
    }
}
