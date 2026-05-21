package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.ehr.VitalsCreateRequest;
import com.shiro.elysiae.dto.response.medical.VitalsDetails;
import com.shiro.elysiae.dto.response.medical.VitalsSummary;
import com.shiro.elysiae.service.VitalsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vitals")
public class VitalsController {

    private final VitalsService vitalsService;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE')")
    @PostMapping()
    public ResponseEntity<VitalsDetails> getMedicalRecord(
            @Valid @RequestBody VitalsCreateRequest request){
        return ResponseEntity.ok().body(vitalsService.logVitals(request));
    }
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','PATIENT')")
    @GetMapping("/{id}")
    public ResponseEntity<Page<VitalsSummary>> getMedicalRecord(@PathVariable long id,
                                                                Pageable pageable){
        return ResponseEntity.ok().body(vitalsService.getPatientVitals(id,pageable));
    }

}
