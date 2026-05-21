package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.ehr.VitalsCreateRequest;
import com.shiro.elysiae.dto.response.medical.VitalsDetails;
import com.shiro.elysiae.dto.response.medical.VitalsSummary;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.User;
import com.shiro.elysiae.model.ehrnprescriptionsnvitals.Vitals;
import com.shiro.elysiae.model.enums.AuditAction;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.repository.PatientRepository;
import com.shiro.elysiae.repository.UserRepository;
import com.shiro.elysiae.repository.VitalsRepository;
import com.shiro.elysiae.util.VitalsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VitalsService {

    private final VitalsRepository vitalsRepository;
    private final PatientRepository patientRepository;
    private final VitalsMapper vitalsMapper;
    private final AuditService auditService;
    private final UserRepository userRepository;

    public VitalsDetails logVitals(VitalsCreateRequest request) {
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new AppException(ErrorCode.PATIENT_NOT_FOUND));

        long currentUserId = Long.parseLong(auth.getName());
        User recordedBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Vitals vitals = Vitals.builder()
                .patient(patient)
                .recordedBy(recordedBy)  // whoever is logged in — doctor or nurse
                .temperature(request.temperature())
                .bloodPressure(request.bloodPressure())
                .heartRate(request.heartRate())
                .oxygenSat(request.oxygenSat())
                .weightKg(request.weightKg())
                .heightCm(request.heightCm())
                .build();

        vitalsRepository.save(vitals);
        auditService.log(AuditAction.VITALS_LOGGED.name(), patient.getFirstName() + " " + patient.getLastName(), patient.getId());
        return vitalsMapper.toVitalsDetails(vitals);
    }

    public Page<VitalsSummary> getPatientVitals(long id, Pageable pageable) {
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
        if(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            validate(id);
        }
        patientRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND)
        );
        return vitalsRepository.findByPatientId(id,pageable).map(vitalsMapper::ToVitalsSummary);
    }

    private void validate(long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        long currentUserId = Long.parseLong(auth.getName());
        boolean isSelf = currentUserId == id;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isSelf && !isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

}
