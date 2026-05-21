package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.pharmacy.DispenseLogCreateRequest;
import com.shiro.elysiae.dto.request.pharmacy.MedicineCreateRequest;
import com.shiro.elysiae.dto.request.pharmacy.MedicineSearchRequest;
import com.shiro.elysiae.dto.request.pharmacy.MedicineUpdateRequest;
import com.shiro.elysiae.dto.request.wardsandbed.BedAdmitPatientRequest;
import com.shiro.elysiae.dto.response.pharmacy.DispenseLogDetails;
import com.shiro.elysiae.dto.response.pharmacy.DispenseLogSummary;
import com.shiro.elysiae.dto.response.pharmacy.MedicineDetails;
import com.shiro.elysiae.dto.response.pharmacy.MedicineSummary;
import com.shiro.elysiae.dto.response.wardsandbeds.AdmissionDetails;
import com.shiro.elysiae.service.PharmacyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pharmacy")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    @PostMapping("/add-medicine")
    public ResponseEntity<MedicineDetails> addMedicine(@Valid @RequestBody MedicineCreateRequest request) {
        return ResponseEntity.ok().body(pharmacyService.addMedicine(request));
    }

    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    @PostMapping("/search-medicine")
    public ResponseEntity<Page<MedicineSummary>> searchMedicine(@Valid @RequestBody MedicineSearchRequest request
            , Pageable pageable) {
        return ResponseEntity.ok().body(pharmacyService.searchMedicine(request, pageable));
    }

    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    @GetMapping("/medicine/{id}")
    public ResponseEntity<MedicineDetails> getMedicineDetails(@PathVariable long id) {
        return ResponseEntity.ok().body(pharmacyService.getMedicineDetails(id));
    }

    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    @PatchMapping()
    public ResponseEntity<MedicineDetails> updateMedicine(@Valid @RequestBody MedicineUpdateRequest request){
        return ResponseEntity.ok().body(pharmacyService.updateMedicine(request));
    }

    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<MedicineDetails> addStock(@PathVariable long id,@RequestParam Integer stockQuantity){
        return ResponseEntity.ok().body(pharmacyService.addStock(id,stockQuantity));
    }

    @PreAuthorize("hasRole('PHARMACIST')")
    @PostMapping("/dispense")
    public ResponseEntity<DispenseLogDetails> dispenseMedicine(@Valid @RequestBody DispenseLogCreateRequest request) {
        return ResponseEntity.ok().body(pharmacyService.dispenseMedicine(request));
    }

    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN','DOCTOR')")
    @PostMapping("/dispense/{id}")
    public ResponseEntity<Page<DispenseLogSummary>> getDispenseLogs(@PathVariable long id,
                                                                    Pageable pageable) {
        return ResponseEntity.ok().body(pharmacyService.getDispenseLogs(id,pageable));
    }

    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN','DOCTOR')")
    @PostMapping("/dispense")
    public ResponseEntity<Page<DispenseLogSummary>> getDispenseLogs(Pageable pageable) {
        return ResponseEntity.ok().body(pharmacyService.getDispenseLogs(pageable));
    }
}
