package com.shiro.elysiae.dto.response.wardsandbeds;

import com.shiro.elysiae.model.enums.WardType;

import java.util.List;

public record WardsDetails(
        Long id,
        String name,
        WardType type,
        String floor,

        List<BedSummary> beds

) {
}
