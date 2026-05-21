package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.patient.PatientCreateRequest;
import com.shiro.elysiae.dto.request.patient.PatientSearchRequest;
import com.shiro.elysiae.dto.request.patient.PatientUpdateRequest;
import com.shiro.elysiae.dto.response.appointment.AppointmentSummary;
import com.shiro.elysiae.dto.response.billing.InvoiceSummary;
import com.shiro.elysiae.dto.response.medical.MedicalRecordSummary;
import com.shiro.elysiae.dto.response.patient.PatientDetails;
import com.shiro.elysiae.dto.response.patient.PatientSummary;
import com.shiro.elysiae.model.enums.Role;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.service.PatientService;
import com.shiro.elysiae.service.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final ReceiptService receiptService;

    @PreAuthorize("hasAnyRole('ADMIN','NURSE','RECEPTIONIST','DOCTOR')")
    @GetMapping
    public ResponseEntity<Page<PatientSummary>> getPatients(
            @Valid @RequestBody PatientSearchRequest patientSearchRequest
            ,Pageable pageable) {
        return ResponseEntity.ok().body(patientService.getAllPatients(patientSearchRequest,pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<PatientDetails> findPatientById(@PathVariable long id) {
        return ResponseEntity.ok().body(patientService.findPatientById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @GetMapping("/{id}/medical-record")
    public ResponseEntity<Page<MedicalRecordSummary>> getMedicalRecords(@PathVariable long id, Pageable pageable) {
        return ResponseEntity.ok().body(patientService.getMedicalRecords(id,pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @GetMapping("/{id}/invoice")
    public ResponseEntity<Page<InvoiceSummary>> getPatientInvoice(@PathVariable long id, Pageable pageable) {
        return ResponseEntity.ok().body(patientService.getPatientInvoice(id,pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @GetMapping("/{id}/appointments")
    public ResponseEntity<Page<AppointmentSummary>> getAppointmentHistory(@PathVariable long id, Pageable pageable) {
        return ResponseEntity.ok().body(patientService.getAppointmentHistory(id,pageable));
    }


    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @GetMapping("/me/medical-record")
    public ResponseEntity<Page<MedicalRecordSummary>> getCurrentPatientMedicalRecord(Pageable pageable) {
        return ResponseEntity.ok().body(patientService.getMedicalRecords(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @GetMapping("/me/invoice")
    public ResponseEntity<Page<InvoiceSummary>> getCurrentPatientInvoice(Pageable pageable) {
        return ResponseEntity.ok().body(patientService.getPatientInvoice(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @GetMapping("/me/appointments")
    public ResponseEntity<Page<AppointmentSummary>> getCurrentPatientAppointments(Pageable pageable) {
        return ResponseEntity.ok().body(patientService.getAppointmentHistory(pageable));
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<PatientDetails> getCurrentPatient() {
        return ResponseEntity.ok().body(patientService.getCurrentPatient());
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}")
    public ResponseEntity<PatientDetails> updatePatient(@PathVariable long id
            ,@Valid @RequestBody PatientUpdateRequest request) {
        return ResponseEntity.ok().body(patientService.updatePatient(id,request));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @PostMapping
    public ResponseEntity<String> createPatient(@Valid @RequestBody PatientCreateRequest request) {
        Patient patient = patientService.registerPatient(request);
        String tempPassword = patient.getUser().getTempPassword();
        String slip = receiptService.generatePatientCredentialSlip(patient, tempPassword);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(slip);
    }
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @GetMapping("/{id}/slip")
    public ResponseEntity<String> reprintReceipt(@PathVariable long id) {
        String  slip = receiptService.reprintCredentialSlip(id, Role.PATIENT);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(slip);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePatient(@PathVariable long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok().body("Deleted Successfully");
    }
}
