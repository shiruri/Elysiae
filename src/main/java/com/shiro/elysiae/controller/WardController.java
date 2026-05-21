package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.wardsandbed.BedAddRequest;
import com.shiro.elysiae.dto.request.wardsandbed.WardCreateRequest;
import com.shiro.elysiae.dto.request.wardsandbed.WardSearchRequest;
import com.shiro.elysiae.dto.response.wardsandbeds.BedDetails;
import com.shiro.elysiae.dto.response.wardsandbeds.BedSummary;
import com.shiro.elysiae.dto.response.wardsandbeds.WardsDetails;
import com.shiro.elysiae.dto.response.wardsandbeds.WardsSummary;
import com.shiro.elysiae.model.enums.BedStatus;
import com.shiro.elysiae.service.WardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/wards")
public class WardController {

    private final WardService wardService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register/ward")
    public ResponseEntity<WardsDetails> registerWard(@Valid @RequestBody WardCreateRequest request){
       return ResponseEntity.ok().body(wardService.registerWard(request));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add/bed")
    public ResponseEntity<BedDetails> addBed(@Valid @RequestBody BedAddRequest request){
        return ResponseEntity.ok().body(wardService.addBed(request));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    @PostMapping("/search")
    public ResponseEntity<Page<WardsSummary>> searchWards(@Valid @RequestBody WardSearchRequest request,
                                                          Pageable pageable){
        return ResponseEntity.ok().body(wardService.getAllWards(request,pageable));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    @GetMapping("/beds/{id}")
    public ResponseEntity<Page<BedSummary>> getAllBeds(@PathVariable("id") long wardId,
                                                     BedStatus status, Pageable pageable){
        return ResponseEntity.ok().body(wardService.getAllBeds(wardId,status,pageable));
    }
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    @GetMapping("/bed/{id}")
    public ResponseEntity<BedDetails> getBedDetails(@PathVariable long id){
        return ResponseEntity.ok().body(wardService.getBedDetails(id));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN','DOCTOR','NURSE')")
    @GetMapping("/ward/{id}")
    public ResponseEntity<WardsDetails> getWardDetails(@PathVariable long id){
        return ResponseEntity.ok().body(wardService.getWardDetails(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/ward/{id}")
    public ResponseEntity<Void> deleteWard(@PathVariable long id) {
        wardService.deleteWard(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/bed/{id}")
    public ResponseEntity<Void> deleteBed(@PathVariable long id) {
        wardService.deleteBed(id);
        return ResponseEntity.noContent().build();
    }



}
