package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.ehr.MedicalRecordCreateRequest;
import com.shiro.elysiae.dto.request.ehr.MedicalRecordUpdateRequest;
import com.shiro.elysiae.dto.request.ehr.PrescriptionCreateRequest;
import com.shiro.elysiae.dto.response.medical.MedicalRecordDetails;
import com.shiro.elysiae.service.RecordsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
public class RecordsController {

    private final RecordsService recordsService;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PostMapping("/create")
    public ResponseEntity<MedicalRecordDetails> createMedicalRecord(@Valid @RequestBody MedicalRecordCreateRequest request){
        return ResponseEntity.ok().body(recordsService.createMedicalRecord(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE')")
    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordDetails> getMedicalRecord(@PathVariable long id){
        return ResponseEntity.ok().body(recordsService.getMedicalRecord(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PatchMapping()
    public ResponseEntity<MedicalRecordDetails> updateMedicalRecord(@Valid @RequestBody MedicalRecordUpdateRequest request){
        return ResponseEntity.ok().body(recordsService.updateMedicalRecord(request));
    }
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PatchMapping("/prescription")
    public ResponseEntity<MedicalRecordDetails> addPrescription(@Valid @RequestBody PrescriptionCreateRequest request){
        return ResponseEntity.ok().body(recordsService.addPrescription(request));
    }




}
