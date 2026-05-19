package com.shiro.elysiae.dto.response.doctor;

import com.shiro.elysiae.model.enums.DayOfWeek;

import java.time.LocalTime;

public record DoctorScheduleResponse(

        Long id,
        Long doctorId,
        String doctorFullName,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        int slotDurationMinutes,
        int totalSlots

) {
    public static DoctorScheduleResponse from(com.shiro.elysiae.model.doctorsndepartment.DoctorSchedule schedule) {
        int totalMinutes = (int) java.time.Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes();
        int slots = totalMinutes / schedule.getSlotDurationMinutes();

        return new DoctorScheduleResponse(
                schedule.getId(),
                schedule.getDoctor().getId(),
                "Dr. " + schedule.getDoctor().getFirstName() + " " + schedule.getDoctor().getLastName(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getSlotDurationMinutes(),
                slots
        );
    }
}