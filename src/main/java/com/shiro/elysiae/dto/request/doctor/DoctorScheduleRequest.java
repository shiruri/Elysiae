package com.shiro.elysiae.dto.request.doctor;

import com.shiro.elysiae.model.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record DoctorScheduleRequest(
        @NotNull(message = "Doctor ID is required")
        Long doctorId,

        @NotNull(message = "Day of week is required")
        DayOfWeek dayOfWeek,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        Integer slotDurationMinutes
) {}