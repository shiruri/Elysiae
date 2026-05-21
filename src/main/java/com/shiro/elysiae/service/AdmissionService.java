package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.wardsandbed.AdmissionTransferRequest;
import com.shiro.elysiae.dto.request.wardsandbed.BedAdmitPatientRequest;
import com.shiro.elysiae.dto.response.wardsandbeds.AdmissionDetails;
import com.shiro.elysiae.dto.response.wardsandbeds.AdmissionSummary;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.enums.AdmissionStatus;
import com.shiro.elysiae.model.enums.AuditAction;
import com.shiro.elysiae.model.enums.BedStatus;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.model.wardsbedsadmission.Admission;
import com.shiro.elysiae.model.wardsbedsadmission.Bed;
import com.shiro.elysiae.repository.*;
import com.shiro.elysiae.util.BedMapper;
import com.shiro.elysiae.util.WardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
@Service
@RequiredArgsConstructor
public class AdmissionService {
    private final PatientRepository patientRepository;
    private final WardRepository wardRepository;
    private final BedRepository bedRepository;
    private final WardMapper wardMapper;
    private final BedMapper bedMapper;
    private final AdmissionRepository admissionRepository;
    private final DoctorRepository doctorRepository;
    private final AuditService auditService;

    public AdmissionDetails admitPatient(BedAdmitPatientRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new AppException(ErrorCode.PATIENT_NOT_FOUND));
        if (patient.getDeletedAt() != null) {
            throw new AppException(ErrorCode.PATIENT_NOT_FOUND);
        }
        Bed bed = bedRepository.findById(request.bedId())
                .orElseThrow(() -> new AppException(ErrorCode.BED_NOT_FOUND));
        if (bed.getDeletedAt() != null) {
            throw new AppException(ErrorCode.BED_NOT_FOUND);
        }
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
        if (doctor.getDeletedAt() != null) {
            throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
        }

        if (bed.getStatus() == BedStatus.OCCUPIED)
            throw new AppException(ErrorCode.BED_NOT_AVAILABLE);

        if (admissionRepository.existsByPatientIdAndStatus(request.patientId(), AdmissionStatus.ADMITTED))
            throw new AppException(ErrorCode.PATIENT_ALREADY_ADMITTED);


        bed.setStatus(BedStatus.OCCUPIED);
        bedRepository.save(bed);

        Admission admission = Admission.builder()
                .patient(patient)
                .bed(bed)
                .admittingDoctor(doctor)
                .admittedAt(LocalDateTime.now())
                .build();
        auditService.log(AuditAction.PATIENT_ADMITTED.name(), admission.getPatient().getFirstName()
                        + " " + admission.getPatient().getFirstName(),
                admission.getPatient().getId());
        return AdmissionDetails.from(admissionRepository.save(admission));
    }
    @Transactional(readOnly = true)
    public Page<AdmissionSummary> getAdmissionByDoctorId(long id, Pageable pageable) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
        if (doctor.getDeletedAt() != null) {
            throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
        }
        return admissionRepository.findByAdmittingDoctorId(
                doctor.getId(),
                pageable
        ).map(AdmissionSummary::from);
    }
    @Transactional(readOnly = true)
    public Page<AdmissionSummary> getAdmissionByPatientId(long id, Pageable pageable) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PATIENT_NOT_FOUND));
        if (patient.getDeletedAt() != null) {
            throw new AppException(ErrorCode.PATIENT_NOT_FOUND);
        }
        return admissionRepository.findByAdmissionFromPatientId(
                patient.getId(),
                pageable
        ).map(AdmissionSummary::from);
    }
    @Transactional(readOnly = true)
    public AdmissionDetails getAdmissionDetails(long id) {
        Admission admission = admissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ADMISSION_NOT_FOUND));
        return AdmissionDetails.from(admission);
    }
    public AdmissionDetails dischargePatient(long id,String diagnosis) {
        Admission admission = admissionRepository.findTopByPatientIdAndStatusOrderByAdmittedAtDesc(id,AdmissionStatus.ADMITTED)
                .orElseThrow(() -> new AppException(ErrorCode.ADMISSION_NOT_FOUND));
        Bed bed = bedRepository.findById(admission.getBed().getId()).orElseThrow(
                () -> new AppException(ErrorCode.BED_NOT_FOUND));
        if (bed.getDeletedAt() != null) {
            throw new AppException(ErrorCode.BED_NOT_FOUND);
        }
        admission.setDischargedAt(LocalDateTime.now());
        admission.setDiagnosis(diagnosis);
        admission.setStatus(AdmissionStatus.DISCHARGED);

        bed.setStatus(BedStatus.AVAILABLE);
        bedRepository.save(bed);
        auditService.log(AuditAction.PATIENT_DISCHARGED.name(), admission.getPatient().getFirstName()
                        + " " + admission.getPatient().getFirstName(),
                admission.getPatient().getId());
        return AdmissionDetails.from(admissionRepository.save(admission));
    }

    public AdmissionDetails transferPatient(AdmissionTransferRequest request) {
        Admission currentAdmission = admissionRepository
                .findTopByPatientIdAndStatusOrderByAdmittedAtDesc(request.patientId(), AdmissionStatus.ADMITTED)
                .orElseThrow(() -> new AppException(ErrorCode.ADMISSION_NOT_FOUND));

        Bed newBed = bedRepository.findById(request.newBedId())
                .orElseThrow(() -> new AppException(ErrorCode.BED_NOT_FOUND));
        if (newBed.getDeletedAt() != null) {
            throw new AppException(ErrorCode.BED_NOT_FOUND);
        }

        if (newBed.getStatus() == BedStatus.OCCUPIED)
            throw new AppException(ErrorCode.BED_NOT_AVAILABLE);

        Doctor doctor = request.newDoctorId() != null
                ? doctorRepository.findById(request.newDoctorId())
                  .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND))
                : currentAdmission.getAdmittingDoctor();
        if (doctor.getDeletedAt() != null) {
            throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
        }

        currentAdmission.getBed().setStatus(BedStatus.AVAILABLE);
        currentAdmission.setStatus(AdmissionStatus.TRANSFERRED);
        bedRepository.save(currentAdmission.getBed());
        admissionRepository.save(currentAdmission);

        newBed.setStatus(BedStatus.OCCUPIED);
        bedRepository.save(newBed);

        Admission newAdmission = Admission.builder()
                .patient(currentAdmission.getPatient())
                .bed(newBed)
                .admittingDoctor(doctor)
                .admittedAt(LocalDateTime.now())
                .build();
        auditService.log(AuditAction.PATIENT_TRANSFERRED.name(), currentAdmission.getPatient().getFirstName()
                + " " + currentAdmission.getPatient().getFirstName(),
                currentAdmission.getPatient().getId());
        return AdmissionDetails.from(admissionRepository.save(newAdmission));
    }





}
