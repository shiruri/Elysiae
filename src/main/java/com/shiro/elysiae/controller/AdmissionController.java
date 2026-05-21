package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.wardsandbed.AdmissionTransferRequest;
import com.shiro.elysiae.dto.request.wardsandbed.BedAdmitPatientRequest;
import com.shiro.elysiae.dto.response.wardsandbeds.AdmissionDetails;
import com.shiro.elysiae.dto.response.wardsandbeds.AdmissionSummary;
import com.shiro.elysiae.service.AdmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admission")
public class AdmissionController {

    private final AdmissionService admissionService;

    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    @PostMapping("/ward/admissions")
    public ResponseEntity<AdmissionDetails> adminPatient(@Valid @RequestBody BedAdmitPatientRequest request){
        return ResponseEntity.ok().body(admissionService.admitPatient(request));
    }
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    @GetMapping("/{doctorId}/doctor")
    public ResponseEntity<Page<AdmissionSummary>> getAdmissionByDoctorId(@PathVariable("doctorId") long id, Pageable pageable){
        return ResponseEntity.ok().body(admissionService.getAdmissionByDoctorId(id,pageable));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE','PATIENT')")
    @GetMapping("/{patientId}/patient")
    public ResponseEntity<Page<AdmissionSummary>> getAdmissionByPatientId(@PathVariable("patientId") long id, Pageable pageable){
        return ResponseEntity.ok().body(admissionService.getAdmissionByPatientId(id,pageable));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    @PostMapping("/{id}")
    public ResponseEntity<AdmissionDetails> getAdmissionDetails(@PathVariable("id") long id){
        return ResponseEntity.ok().body(admissionService.getAdmissionDetails(id));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    @PatchMapping("/{patient}/discharge")
    public ResponseEntity<AdmissionDetails> dischargePatient(@PathVariable("patient") long id,
                                                             @RequestParam String diagnosis){
        return ResponseEntity.ok().body(admissionService.dischargePatient(id,diagnosis));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    @PatchMapping("/transfer")
    public ResponseEntity<AdmissionDetails> transferPatient(@RequestBody @Valid AdmissionTransferRequest request){
        return ResponseEntity.ok().body(admissionService.transferPatient(request));
    }
}
