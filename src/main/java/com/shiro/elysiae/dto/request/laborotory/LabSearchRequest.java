package com.shiro.elysiae.dto.request.laborotory;

import com.shiro.elysiae.model.enums.LabPriority;
import com.shiro.elysiae.model.enums.LabRequestStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public record LabSearchRequest(
        Long patientId,
        Long doctorId,
        LabRequestStatus status,
        LabPriority priority
) {
}
