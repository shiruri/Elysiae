package com.shiro.elysiae.dto.request.appointment;



import java.time.LocalDate;

public record AppointmentSlotsRequest(
        Long doctorId,
        LocalDate date
) {
}
