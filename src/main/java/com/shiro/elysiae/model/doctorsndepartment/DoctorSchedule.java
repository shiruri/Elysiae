package com.shiro.elysiae.model.doctorsndepartment;

import com.shiro.elysiae.model.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(
        name = "doctor_schedules",
        indexes = {
                @Index(name = "idx_schedules_doctor_day", columnList = "doctor_id, day_of_week")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, columnDefinition = "ENUM('MON','TUE','WED','THU','FRI','SAT','SUN')")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_duration_minutes")
    @Builder.Default
    private Integer slotDurationMinutes = 30;
}