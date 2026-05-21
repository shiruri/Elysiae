package com.shiro.elysiae.dto.response.wardsandbeds;

import com.shiro.elysiae.model.enums.BedStatus;

public record BedDetails(
        Long id,
        String bedNo,
        BedStatus status,


        // =========================================================
        // RELATIONSHIPS
        // =========================================================

        WardsSummary ward

) {
}
