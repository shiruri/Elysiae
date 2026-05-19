package com.shiro.elysiae.dto.response.doctor;

import com.shiro.elysiae.model.enums.DayOfWeek;

import java.util.List;
import java.util.Map;

public record DoctorWeeklyScheduleResponse(
        Long doctorId,
        String doctorFullName,
        Map<DayOfWeek, List<DoctorScheduleResponse>> scheduleByDay
) {}