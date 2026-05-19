package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.department.DepartmentCreateRequest;
import com.shiro.elysiae.dto.request.department.DepartmentUpdateRequest;
import com.shiro.elysiae.dto.request.doctor.*;
import com.shiro.elysiae.dto.response.appointment.AppointmentSummary;
import com.shiro.elysiae.dto.response.department.DepartmentDetails;
import com.shiro.elysiae.dto.response.doctor.DoctorDetails;
import com.shiro.elysiae.dto.response.doctor.DoctorScheduleResponse;
import com.shiro.elysiae.dto.response.doctor.DoctorSummary;
import com.shiro.elysiae.dto.response.doctor.DoctorWeeklyScheduleResponse;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.User;
import com.shiro.elysiae.model.appointments.Appointment;
import com.shiro.elysiae.model.doctorsndepartment.Department;
import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.doctorsndepartment.DoctorSchedule;
import com.shiro.elysiae.model.enums.AuditAction;
import com.shiro.elysiae.model.enums.DayOfWeek;
import com.shiro.elysiae.model.enums.Gender;
import com.shiro.elysiae.model.enums.Role;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.repository.*;
import com.shiro.elysiae.util.AppointmentMapper;
import com.shiro.elysiae.util.DoctorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AuditService auditService;
    private final DoctorMapper doctorMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    @Transactional(readOnly = true)
    public Page<DoctorSummary> searchDoctors(DoctorSearchRequest request, Pageable pageable) {
        return doctorRepository.searchDoctors(
                        request.doctorId()
                        , request.departmentName()
                        , request.specialization(),
                        pageable)
                .map(doctorMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public DoctorDetails getByDoctorId(long id) {
        return doctorMapper.toDetails(doctorRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.DOCTOR_NOT_FOUND)));
    }

    @Transactional
    public DoctorDetails updateDoctor(long id, DoctorUpdateRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        validate(doctor);

        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
            doctor.setDepartment(department);
        }
        if (request.firstName() != null) {
            if (!request.firstName().isBlank()) {
                doctor.setFirstName(request.firstName());
            }
        }
        if (request.lastName() != null) {
            if (!request.lastName().isBlank()) {
                doctor.setLastName(request.lastName());
            }
        }
        if (request.specialization() != null) {
            if (!request.specialization().isBlank()) {
                doctor.setSpecialization(request.specialization());
            }
        }

        if (request.licenseNumber() != null) {
            if (!request.licenseNumber().isBlank()) {
                doctor.setLicenseNumber(request.licenseNumber());
            }
        }

        if (request.phone() != null) {
            if (!request.phone().isBlank()) {
                doctor.setPhone(request.phone());
            }
        }

        Doctor saved = doctorRepository.save(doctor);
        auditService.log(AuditAction.DOCTOR_UPDATED.name(), saved.getFirstName() + " " + saved.getLastName(), saved.getId());
        return doctorMapper.toDetails(saved);
    }

    @Transactional
    public DoctorDetails registerDoctor(DoctorCreateRequest request) {
        String tempPassword = request.username() + "-" + (1000 + new Random().nextInt(9000));

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(tempPassword))
                .role(Role.DOCTOR)
                .tempPassword(tempPassword)
                .mustChangePassword(true)
                .build();
        userRepository.save(user);

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));


        Doctor doctor = Doctor.builder()
                .user(user)
                .department(department)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .specialization(request.specialization())
                .licenseNumber(request.licenseNumber())
                .phone(request.phone())
                .phone(request.phone())
                .build();
        Doctor saved = doctorRepository.save(doctor);
        auditService.log(AuditAction.DOCTOR_CREATED.name(), saved.getFirstName() + " " + saved.getLastName(), saved.getId());
        return doctorMapper.toDetails(saved);
    }

    @Transactional
    public DoctorScheduleResponse updateDoctorSchedule(long id, DoctorScheduleUpdateRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        validate(doctor);

        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .slotDurationMinutes(request.slotDurationMinutes())
                .build();
        DoctorSchedule saved = doctorScheduleRepository.save(schedule);
        auditService.log(AuditAction.DOCTOR_SCHEDULE_UPDATED.name(), doctor.getFirstName() + " " + doctor.getLastName(), saved.getId());
        return DoctorScheduleResponse.from(saved);
    }

    public Page<AppointmentSummary> getAssignedPatients(long id, Pageable pageable) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
        return appointmentRepository.findAppointmentsByDoctorIdFrom(
                doctor.getId(),
                LocalDateTime.now(),
                pageable
        ).map(appointmentMapper::toSummary);
    }

    public Page<AppointmentSummary> getAssignedPatientsCurrentDoctor(Pageable pageable) {
        Authentication auth = getAuthentication();
        long currentUserId = Long.parseLong(auth.getName());
        Doctor doctor = doctorRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
        return appointmentRepository.findAppointmentsByDoctorIdFrom(
                doctor.getId(),
                LocalDateTime.now(),
                pageable
        ).map(appointmentMapper::toSummary);
    }

    public DoctorWeeklyScheduleResponse getDoctorSchedule(long id, DoctorScheduleRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        Map<DayOfWeek, List<DoctorScheduleResponse>> scheduleByDay = doctorScheduleRepository
                .findByDoctorId(id)
                .stream()
                .map(DoctorScheduleResponse::from)
                .collect(Collectors.groupingBy(DoctorScheduleResponse::dayOfWeek));

        return new DoctorWeeklyScheduleResponse(
                doctor.getId(),
                doctor.getFirstName() + " " + doctor.getLastName(),
                scheduleByDay
        );
    }


    private void validate(Doctor doctor) {
        Authentication auth = getAuthentication();
        long currentUserId = Long.parseLong(auth.getName());
        boolean isSelf = currentUserId == doctor.getUser().getId();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isSelf && !isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}



