package com.shiro.elysiae.dto.response.wardsandbeds;

import com.shiro.elysiae.model.enums.WardType;

import java.util.List;

public record WardsSummary(
        Long id,
        String name,
        WardType type,
        String floor,
        Long totalBeds,
        Long availableBeds,
        Long occupiedBeds
) {
}
