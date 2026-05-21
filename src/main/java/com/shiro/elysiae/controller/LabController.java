package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.laborotory.LabRequestCreateRequest;
import com.shiro.elysiae.dto.request.laborotory.LabResultCreateRequest;
import com.shiro.elysiae.dto.request.laborotory.LabSearchRequest;
import com.shiro.elysiae.dto.response.laborotory.LabRequestDetails;
import com.shiro.elysiae.dto.response.laborotory.LabRequestSummary;
import com.shiro.elysiae.dto.response.laborotory.LabResultDetails;
import com.shiro.elysiae.model.enums.LabRequestStatus;
import com.shiro.elysiae.service.LabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lab")
public class LabController {

    private final LabService labService;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PostMapping("/request")
    public ResponseEntity<LabRequestDetails> makeRequest(@RequestBody LabRequestCreateRequest request) {
        return ResponseEntity.ok(labService.makeRequest(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','LAB_TECH')")
    @PostMapping("/request/search")
    public ResponseEntity<Page<LabRequestSummary>> getLabRequests(@RequestBody LabSearchRequest request
            , Pageable pageable) {
        return ResponseEntity.ok(labService.getLabRequests(request, pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','LAB_TECH','NURSE')")
    @GetMapping("/{id}")
    public ResponseEntity<LabRequestDetails> getLabDetails(@PathVariable long id) {
        return ResponseEntity.ok(labService.getLabDetails(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LAB_TECH')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<LabRequestDetails> updateLabRequestStatus(@PathVariable long id,
                                                                    @RequestParam LabRequestStatus status) {
        return ResponseEntity.ok(labService.updateLabStatus(id,status));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LAB_TECH')")
    @PostMapping("/result")
    public ResponseEntity<LabResultDetails> postLabRequest(@RequestBody @Valid LabResultCreateRequest request) {
        return ResponseEntity.ok(labService.postLabResult(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LAB_TECH')")
    @GetMapping("/{id}/result")
    public ResponseEntity<LabRequestDetails> getLabRequestResult(@PathVariable long id) {
        return ResponseEntity.ok(labService.getLabRequestResult(id));
    }
}
