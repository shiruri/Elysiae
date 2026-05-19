package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.appointment.AppointmentCreateRequest;
import com.shiro.elysiae.dto.request.appointment.AppointmentSlotsRequest;
import com.shiro.elysiae.dto.request.appointment.AppointmentUpdateRequest;
import com.shiro.elysiae.dto.request.appointment.SearchAppointmentRequest;
import com.shiro.elysiae.dto.response.appointment.AppointmentAvailableDates;
import com.shiro.elysiae.dto.response.appointment.AppointmentDetails;
import com.shiro.elysiae.dto.response.appointment.AppointmentSummary;
import com.shiro.elysiae.dto.response.doctor.DoctorSummary;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.appointments.Appointment;
import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.doctorsndepartment.DoctorSchedule;
import com.shiro.elysiae.model.enums.AppointmentStatus;
import com.shiro.elysiae.model.enums.AuditAction;
import com.shiro.elysiae.model.enums.DayOfWeek;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.repository.AppointmentRepository;
import com.shiro.elysiae.repository.DoctorRepository;
import com.shiro.elysiae.repository.DoctorScheduleRepository;
import com.shiro.elysiae.repository.PatientRepository;
import com.shiro.elysiae.util.AppointmentMapper;
import com.shiro.elysiae.util.AppointmentSlotMapper;
import com.shiro.elysiae.util.DoctorMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AuditService auditService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentDetails createAppointment(AppointmentCreateRequest request) {

        Patient patient = patientRepository.findById(request.patientId()).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND));
        Doctor doctor = doctorRepository.findById(request.doctorId()).orElseThrow(
                () -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDateTime(request.appointmentDateTime())
                .type(request.type())
                .createdAt(Instant.now())
                .build();
        Appointment savedAppointment = appointmentRepository.save(appointment);
        auditService.log(AuditAction.APPOINTMENT_BOOKED.name(), "APPOINTMENT", savedAppointment.getId());
        return appointmentMapper.toDetails(savedAppointment);
    }

    public Page<AppointmentSummary> getAppointments(SearchAppointmentRequest request, Pageable pageable) {
        return appointmentRepository.searchAppointments(
                request.patientId()
                , request.doctorId()
                , request.from()
                , request.to()
                , request.type()
                , pageable).map(appointmentMapper::toSummary);
    }

    public Page<AppointmentSummary> getCurrentUserAppointments(Pageable pageable) {
        return appointmentRepository.searchAppointmentsByPatientId(
                getCurrentUserId()
                ,pageable).map(appointmentMapper::toSummary);
    }

    public AppointmentDetails updateAppointment(long id, AppointmentUpdateRequest request) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND)
        );
        if(request.appointmentDate() != null) {
            appointment.setAppointmentDateTime(request.appointmentDate());
        }
        if(request.notes() != null) {
            if(!request.notes().isBlank()) {
                appointment.setNotes(request.notes());
            }
        }
        if(request.type() != null) {
            if(!request.type().name().isBlank()) {
                appointment.setType(request.type());
            }
        }


        auditService.log(AuditAction.APPOINTMENT_UPDATED.name(), "APPOINTMENT", appointment.getId());
        return  appointmentMapper.toDetails(appointmentRepository.save(appointment));
    }
    public AppointmentDetails updateAppointment(long id,LocalDateTime appointmentDateTime) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND)
        );
        if(appointment.getPatient().getId() != getCurrentUserId()) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        if(appointmentDateTime  != null) {
            appointment.setAppointmentDateTime(appointmentDateTime);
        }
        auditService.log(AuditAction.APPOINTMENT_UPDATED.name(), "APPOINTMENT", appointment.getId());
        return  appointmentMapper.toDetails(appointmentRepository.save(appointment));
    }

    public AppointmentDetails getAppointment(long id) {
        return appointmentMapper.toDetails(appointmentRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND)
        ));
    }

    public AppointmentDetails getCurrentUserAppointment(long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND)
        );
        if(appointment.getPatient().getId() != getCurrentUserId()) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        return appointmentMapper.toDetails(appointment);
    }

    public AppointmentDetails updateAppointmentStatus(long id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND)
        );
        appointment.setStatus(status);
        auditService.log(AuditAction.APPOINTMENT_UPDATED.name(), "APPOINTMENT", appointment.getId());
        return  appointmentMapper.toDetails(appointmentRepository.save(appointment));
    }

    public AppointmentDetails updateAppointmentCurrentUserStatus(long id,AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND)
        );
        if(appointment.getPatient().getId() != getCurrentUserId()) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        if(status.name().equalsIgnoreCase("CANCELLED")) {
            appointment.setStatus(status);
        }else {
            throw new AppException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        auditService.log(AuditAction.APPOINTMENT_UPDATED.name(), "APPOINTMENT", appointment.getId());
        return  appointmentMapper.toDetails(appointmentRepository.save(appointment));
    }

    public AppointmentAvailableDates getAvailableDates(AppointmentSlotsRequest slotsRequest) {

        DoctorSchedule schedule = doctorScheduleRepository
                .findByDoctorIdAndDayOfWeek(slotsRequest.doctorId(), DayOfWeek.from(slotsRequest.date().getDayOfWeek()));

        if (schedule == null) {
            Doctor doctor = doctorRepository.findById(slotsRequest.doctorId())
                    .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
            DoctorSummary doctorSummary = doctorMapper.toSummary(doctor);
            return AppointmentSlotMapper.toResponse(doctorSummary, slotsRequest.date(), List.of());
        }
        LocalDateTime startOfDay = slotsRequest.date().atStartOfDay();
        LocalDateTime endOfDay = slotsRequest.date().plusDays(1).atStartOfDay();

        List<Appointment> appointments = appointmentRepository
                .findDoctorAppointmentsForDay(slotsRequest.doctorId(), startOfDay, endOfDay);

        Set<LocalDateTime> bookedSlots = appointments.stream()
                .map(Appointment::getAppointmentDateTime)
                .collect(Collectors.toSet());

        List<LocalDateTime> availableSlots = generateSlots(
                slotsRequest.date(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getSlotDurationMinutes()
        ).stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());

        Doctor doctor = doctorRepository.findById(slotsRequest.doctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));
        DoctorSummary doctorSummary = doctorMapper.toSummary(doctor);

        return AppointmentSlotMapper.toResponse(doctorSummary, slotsRequest.date(), availableSlots);
    }

    private List<LocalDateTime> generateSlots(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            int slotDurationMinutes
    ) {
        List<LocalDateTime> slots = new ArrayList<>();
        LocalDateTime current = date.atTime(startTime);
        LocalDateTime end = date.atTime(endTime);

        while (current.isBefore(end)) {
            slots.add(current);
            current = current.plusMinutes(slotDurationMinutes);
        }

        return slots;
    }

    private long getCurrentUserId() {
        Authentication auth =SecurityContextHolder.getContext().getAuthentication();
        if(auth == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        return Long.parseLong(auth.getName());
    }

}
