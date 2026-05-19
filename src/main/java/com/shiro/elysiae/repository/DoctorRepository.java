package com.shiro.elysiae.repository;

import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
