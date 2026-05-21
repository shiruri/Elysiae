package com.shiro.elysiae.dto.response.laborotory;

import com.shiro.elysiae.model.enums.LabPriority;
import com.shiro.elysiae.model.enums.LabRequestStatus;
import java.time.LocalDateTime;

public record LabRequestSummary(

        Long id,
        String patientFullName,
        String testType,
        LabPriority priority,
        LabRequestStatus status,
        LocalDateTime requestedAt

) {}