package com.shiro.elysiae.dto.response.dashboard;

import java.math.BigDecimal;

public record DashboardResponse(
        long totalPatients,
        long totalDoctors,
        long appointmentsToday,
        BigDecimal revenueToday,
        BigDecimal totalRevenue,
        long availableBeds,
        long occupiedBeds,
        long totalBeds
) {}
