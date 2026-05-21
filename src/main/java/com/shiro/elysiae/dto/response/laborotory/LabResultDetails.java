package com.shiro.elysiae.dto.response.laborotory;

import java.time.LocalDateTime;

public record LabResultDetails(

        Long id,
        String resultValue,
        String normalRange,
        Boolean isAbnormal,
        String remarks,
        String performedBy,
        LocalDateTime performedAt

) {}