package com.shiro.elysiae.dto.response.laborotory;

import com.shiro.elysiae.model.enums.LabPriority;
import com.shiro.elysiae.model.enums.LabRequestStatus;
import java.time.LocalDateTime;

public record LabRequestDetails(

        Long id,

        Long patientId,
        String patientFullName,

        Long doctorId,
        String doctorFullName,

        String testType,
        LabPriority priority,
        LabRequestStatus status,
        LocalDateTime requestedAt,

        LabResultDetails result

) {}