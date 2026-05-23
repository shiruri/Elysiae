package com.shiro.elysiae.dto.request.laborotory;

import com.shiro.elysiae.model.enums.LabPriority;
import com.shiro.elysiae.model.enums.LabRequestStatus;

public record LabSearchRequest(
        Long patientId,
        Long doctorId,
        LabRequestStatus status,
        LabPriority priority
) {
}
