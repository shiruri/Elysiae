package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.laborotory.LabRequestCreateRequest;
import com.shiro.elysiae.dto.request.laborotory.LabResultCreateRequest;
import com.shiro.elysiae.dto.request.laborotory.LabSearchRequest;
import com.shiro.elysiae.dto.response.laborotory.LabRequestDetails;
import com.shiro.elysiae.dto.response.laborotory.LabRequestSummary;
import com.shiro.elysiae.dto.response.laborotory.LabResultDetails;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.enums.AuditAction;
import com.shiro.elysiae.model.enums.LabRequestStatus;
import com.shiro.elysiae.model.laborotory.LabRequest;
import com.shiro.elysiae.model.laborotory.LabResult;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.repository.DoctorRepository;
import com.shiro.elysiae.repository.LabRequestRepository;
import com.shiro.elysiae.repository.LabResultRepository;
import com.shiro.elysiae.repository.PatientRepository;
import com.shiro.elysiae.util.LabMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class LabService {

    private final LabRequestRepository labRequestRepository;
    private final LabResultRepository labResultRepository;
    private final LabMapper labMapper;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AuditService auditService;

    public LabRequestDetails makeRequest(LabRequestCreateRequest request) {
        Patient patient = patientRepository.findById(request.patientId()).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND));
        Doctor doctor = doctorRepository.findById(request.doctorId()).orElseThrow(
                () -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        LabRequest labRequest = LabRequest.builder()
                .patient(patient)
                .doctor(doctor)
                .testType(request.testType())
                .priority(request.priority())
                .build();
        auditService.log(String.valueOf(AuditAction.LAB_REQUEST_CREATED),patient.getFirstName()+
                " "+ patient.getLastName(),patient.getId());
        return labMapper.toDetails(labRequestRepository.save(labRequest));
    }

    public LabRequestDetails updateLabStatus(long id, LabRequestStatus status) {
        LabRequest labRequest = labRequestRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.LAB_REQUEST_NOT_FOUND)
        );
        labRequest.setStatus(status);
        LabResult result = labResultRepository.findByLabRequestId(id).orElse(null);

        auditService.log(String.valueOf(AuditAction.LAB_REQUEST_UPDATED),labRequest.getPatient().getFirstName() +
                " " + labRequest.getPatient().getLastName(),labRequest.getId());

        return labMapper.toDetails(labRequestRepository.save(labRequest),result);
    }

    @Transactional(readOnly = true)
    public LabRequestDetails getLabDetails(long id) {
        LabResult result = labResultRepository.findByLabRequestId(id).orElse(null);
        return labMapper.toDetails(labRequestRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.LAB_REQUEST_NOT_FOUND)
        ), result);
    }
    @Transactional(readOnly = true)
    public Page<LabRequestSummary> getLabRequests(LabSearchRequest request, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Long doctorId = null;

        boolean isDoctor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        if (isDoctor) {
            long currentUserId = Long.parseLong(auth.getName());
            Doctor doctor = doctorRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
            doctorId = doctor.getId();
        }

        return labRequestRepository.search(
                request.patientId() != null ? request.patientId() : 0L,
                doctorId != null ? doctorId : (request.doctorId() != null ? request.doctorId() : 0L),
                request.status(),
                request.priority(),
                pageable
        ).map(labMapper::toSummary);
    }

    public LabResultDetails postLabResult(LabResultCreateRequest request) {
        LabRequest labRequest = labRequestRepository.findById(request.labRequestId()).orElseThrow(
                () -> new AppException(ErrorCode.LAB_REQUEST_NOT_FOUND)
        );
        LabResult labResult = LabResult.builder()
                .labRequest(labRequest)
                .resultValue(request.resultValue())
                .normalRange(request.normalRange())
                .isAbnormal(request.isAbnormal())
                .remarks(request.remarks())
                .build();
        auditService.log(String.valueOf(AuditAction.LAB_RESULT_ENTERED),labRequest.getPatient().getFirstName()+
                " "+ labRequest.getPatient().getLastName(),labRequest.getPatient().getId());
        return labMapper.toResultDetails(labResultRepository.save(labResult));
    }

    @Transactional(readOnly = true)
    public LabRequestDetails getLabRequestResult(long id) {
        LabRequest labRequest = labRequestRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.LAB_REQUEST_NOT_FOUND)
        );
        LabResult labResult = labResultRepository.findByLabRequestId(id).orElse(null);

        return labMapper.toDetails(labRequest,labResult);
    }
}
