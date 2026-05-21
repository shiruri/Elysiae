package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.doctor.*;
import com.shiro.elysiae.dto.response.appointment.AppointmentSummary;
import com.shiro.elysiae.dto.response.doctor.DoctorDetails;
import com.shiro.elysiae.dto.response.doctor.DoctorScheduleResponse;
import com.shiro.elysiae.dto.response.doctor.DoctorSummary;
import com.shiro.elysiae.dto.response.doctor.DoctorWeeklyScheduleResponse;
import com.shiro.elysiae.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService doctorService;


    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST','PATIENT','NURSE')")
    @PostMapping()
    public ResponseEntity<Page<DoctorSummary>> getAllDoctors(@RequestBody
                                                             DoctorSearchRequest request, Pageable pageable) {
        return ResponseEntity.ok().body(doctorService.searchDoctors(request, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<DoctorDetails> registerDoctor(@Valid @RequestBody DoctorCreateRequest request) {
        return ResponseEntity.ok().body(doctorService.registerDoctor(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PatchMapping("/update/{id}")
    public ResponseEntity<DoctorDetails> updateDoctor(@PathVariable long id,
                                                      @Valid @RequestBody DoctorUpdateRequest request) {
        return ResponseEntity.ok().body(doctorService.updateDoctor(id,request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST','PATIENT','NURSE')")
    @PatchMapping("/update/{id}/schedule")
    public ResponseEntity<DoctorScheduleResponse> updateDoctorSchedule(@PathVariable long id,
                                                                       @Valid @RequestBody DoctorScheduleUpdateRequest request) {
        return ResponseEntity.ok().body(doctorService.updateDoctorSchedule(id,request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST','PATIENT')")
    @PostMapping("/{id}/schedule")
    public ResponseEntity<DoctorWeeklyScheduleResponse> getDoctorWeeklySchedule(@PathVariable long id) {
        return ResponseEntity.ok().body(doctorService.getDoctorSchedule(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE')")
    @GetMapping("/{id}/patients")
    public ResponseEntity<Page<AppointmentSummary>> getAssignedPatients(@PathVariable long id, Pageable pageable) {
        return ResponseEntity.ok().body(doctorService.getAssignedPatients(id,pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/me/patients")
    public ResponseEntity<Page<AppointmentSummary>> getAssignedPatientsCurrentDoctor(Pageable pageable) {
        return ResponseEntity.ok().body(doctorService.getAssignedPatientsCurrentDoctor(pageable));
    }
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST','PATIENT','NURSE')")
    @GetMapping("/{id}")
    public ResponseEntity<DoctorDetails> getDoctorById(@PathVariable long id) {
        return ResponseEntity.ok().body(doctorService.getByDoctorId(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

}
