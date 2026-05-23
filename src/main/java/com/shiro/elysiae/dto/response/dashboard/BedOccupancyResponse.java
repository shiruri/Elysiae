package com.shiro.elysiae.dto.response.dashboard;

import com.shiro.elysiae.model.enums.WardType;

public record BedOccupancyResponse(
        String wardName,
        WardType wardType,
        String floor,
        long totalBeds,
        long availableBeds,
        long occupiedBeds,
        long maintenanceBeds,
        double occupancyRate  // percentage
) {}