package com.shiro.elysiae.util;


import com.shiro.elysiae.dto.response.appointment.AppointmentAvailableDates;
import com.shiro.elysiae.dto.response.doctor.DoctorSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AppointmentSlotMapper  {

    public static AppointmentAvailableDates toResponse(
            DoctorSummary doctor,
            LocalDate date,
            List<LocalDateTime> slots
    ) {
        return new AppointmentAvailableDates(doctor, date, slots);
    }
}