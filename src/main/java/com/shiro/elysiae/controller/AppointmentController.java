package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.appointment.AppointmentCreateRequest;
import com.shiro.elysiae.dto.request.appointment.AppointmentSlotsRequest;
import com.shiro.elysiae.dto.request.appointment.AppointmentUpdateRequest;
import com.shiro.elysiae.dto.request.appointment.SearchAppointmentRequest;
import com.shiro.elysiae.dto.response.appointment.AppointmentAvailableDates;
import com.shiro.elysiae.dto.response.appointment.AppointmentDetails;
import com.shiro.elysiae.dto.response.appointment.AppointmentSummary;
import com.shiro.elysiae.model.enums.AppointmentStatus;
import com.shiro.elysiae.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequestMapping("/api/appointments")
@RestController
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT','RECEPTIONIST')")
    @PostMapping("/create")
    public ResponseEntity<AppointmentDetails> createAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        return ResponseEntity.ok(appointmentService.createAppointment(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    @PostMapping()
    public ResponseEntity<Page<AppointmentSummary>> getAppointments(@RequestBody SearchAppointmentRequest request,
                                                                    Pageable pageable) {
        return ResponseEntity.ok().body(appointmentService.getAppointments(request, pageable));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<Page<AppointmentSummary>> getCurrentUserAppointments(Pageable pageable) {
        return ResponseEntity.ok().body(appointmentService.getCurrentUserAppointments(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    @PatchMapping("/{id}")
    public ResponseEntity<AppointmentDetails> updateAppointment(@PathVariable long id,
                                                                      @RequestBody AppointmentUpdateRequest request) {
        return ResponseEntity.ok().body(appointmentService.updateAppointment(id, request));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/me/{id}")
    public ResponseEntity<AppointmentDetails> updateAppointment(@PathVariable long id,
                                                                @RequestParam LocalDateTime appointmentDateTime) {
        return ResponseEntity.ok().body(appointmentService.updateAppointment(id,appointmentDateTime));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDetails> getAppointment(@PathVariable long id) {
        return ResponseEntity.ok().body(appointmentService.getAppointment(id));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/{id}")
    public ResponseEntity<AppointmentDetails> getCurrentUserAppointment(@PathVariable long id) {
        return ResponseEntity.ok().body(appointmentService.getCurrentUserAppointment(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentDetails> updateAppointmentStatus(@PathVariable long id,@RequestParam AppointmentStatus status) {
        return ResponseEntity.ok().body(appointmentService.updateAppointmentStatus(id,status));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/me/{id}/status")
    public ResponseEntity<AppointmentDetails> updateAppointmentCurrentUserStatus(@PathVariable long id,
                                                                      @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok().body(appointmentService.updateAppointmentCurrentUserStatus(id,status));
    }
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    @GetMapping("/slot")
    public ResponseEntity<AppointmentAvailableDates> getDoctorAvailableSlot(@RequestParam AppointmentSlotsRequest request) {
        return ResponseEntity.ok().body(appointmentService.getAvailableDates(request));
    }
}
