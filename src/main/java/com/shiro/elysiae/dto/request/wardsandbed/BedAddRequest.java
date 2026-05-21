package com.shiro.elysiae.dto.request.wardsandbed;

import com.shiro.elysiae.model.enums.BedStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BedAddRequest(
        @NotNull
        Long wardId,
        @NotNull
        @Size(min = 1, max = 20)
        String bedNo,
        @NotNull
        BedStatus status
) {
}
