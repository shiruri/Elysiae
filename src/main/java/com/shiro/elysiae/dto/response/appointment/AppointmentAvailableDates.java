package com.shiro.elysiae.dto.response.appointment;

import com.shiro.elysiae.dto.response.doctor.DoctorSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AppointmentAvailableDates(
        DoctorSummary doctor,
        LocalDate date,
        List<LocalDateTime> availableSlots
) {}