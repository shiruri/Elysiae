package com.shiro.elysiae.dto.response.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueReportResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalRevenue,
        BigDecimal totalPaid,
        BigDecimal totalUnpaid,
        long totalInvoices,
        long paidInvoices,
        long unpaidInvoices,
        long partialInvoices
) {}