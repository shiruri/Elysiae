package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.doctorsndepartment.DoctorSchedule;
import com.shiro.elysiae.model.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule,Long> {

    DoctorSchedule findByDoctorIdAndDayOfWeek(long id, DayOfWeek dayOfWeek);
}
