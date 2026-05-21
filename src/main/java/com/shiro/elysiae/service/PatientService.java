package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.patient.PatientCreateRequest;
import com.shiro.elysiae.dto.request.patient.PatientSearchRequest;
import com.shiro.elysiae.dto.request.patient.PatientUpdateRequest;
import com.shiro.elysiae.dto.response.appointment.AppointmentSummary;
import com.shiro.elysiae.dto.response.billing.InvoiceSummary;
import com.shiro.elysiae.dto.response.medical.MedicalRecordSummary;
import com.shiro.elysiae.dto.response.patient.PatientDetails;
import com.shiro.elysiae.dto.response.patient.PatientSummary;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.User;
import com.shiro.elysiae.model.enums.AuditAction;
import com.shiro.elysiae.model.enums.Gender;
import com.shiro.elysiae.model.enums.Role;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.repository.*;
import com.shiro.elysiae.util.AppointmentMapper;
import com.shiro.elysiae.util.InvoiceMapper;
import com.shiro.elysiae.util.MedicalRecordMapper;
import com.shiro.elysiae.util.PatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@RequiredArgsConstructor
@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final AuditService auditService;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordMapper medicalRecordMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public Page<PatientSummary> getAllPatients(PatientSearchRequest patientSearchRequest,
                                               Pageable pageable) {
        Integer ageFrom = patientSearchRequest.ageFrom() == null ? null : patientSearchRequest.ageFrom();
        Integer ageTo = patientSearchRequest.ageTo() == null ? null : patientSearchRequest.ageTo();

        return patientRepository
                .searchPatients(
                        patientSearchRequest.keyword(),
                        patientSearchRequest.gender(),
                        patientSearchRequest.bloodType(),
                        ageFrom,
                        ageTo,
                        pageable
                )
                .map(patientMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<MedicalRecordSummary> getMedicalRecords(long id, Pageable pageable) {
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND)
        );
        if (patient.getDeletedAt() != null) {
            throw new AppException(ErrorCode.PATIENT_NOT_FOUND);
        }

        return medicalRecordRepository
                .findByPatientId(patient.getId(), pageable).map(medicalRecordMapper::toRecordSummary);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceSummary> getPatientInvoice(long id, Pageable pageable) {
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND)
        );
        if (patient.getDeletedAt() != null) {
            throw new AppException(ErrorCode.PATIENT_NOT_FOUND);
        }

        return invoiceRepository
                .findByPatientId(patient.getId(), pageable).map(invoiceMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentSummary> getAppointmentHistory(long id, Pageable pageable) {
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND)
        );
        if (patient.getDeletedAt() != null) {
            throw new AppException(ErrorCode.PATIENT_NOT_FOUND);
        }

        Page<AppointmentSummary> appointments = appointmentRepository
                .findByPatientId(patient.getId(), pageable).map(appointmentMapper::toSummary);

        return appointments;
    }

    @Transactional(readOnly = true)
    public PatientDetails findPatientById(long id) {
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND)
        );
        if (patient.getDeletedAt() != null) {
            throw new AppException(ErrorCode.PATIENT_NOT_FOUND);
        }

        return patientMapper.toDetails(patient);
    }

    @Transactional
    public Patient registerPatient(PatientCreateRequest request) {
        String tempPassword = request.username() + "-" + (1000 + new Random().nextInt(9000));

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(tempPassword))
                .role(Role.PATIENT)
                .mustChangePassword(true)
                .tempPassword(tempPassword)
                .build();
        userRepository.save(user);

        Patient patient = Patient.builder()
                .user(user)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .gender(Gender.valueOf(request.gender()))
                .bloodType(request.bloodType())
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .emergencyContactName(request.emergencyContactName())
                .emergencyContactPhone(request.emergencyContactPhone())

                .build();
        Patient saved = patientRepository.save(patient);
        auditService.log(AuditAction.PATIENT_UPDATED.name(), saved.getFirstName() + " " + saved.getLastName(), saved.getId());
        return patient;
    }

    @Transactional(readOnly = true)
    public Page<MedicalRecordSummary> getMedicalRecords(Pageable pageable) {
        Authentication auth = getAuthentication();
        long id = Long.parseLong(auth.getName());
        Patient patient = patientRepository.findByUser_Id(id).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND)
        );
        return medicalRecordRepository
                .findByPatientId(patient.getId(), pageable).map(medicalRecordMapper::toRecordSummary);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentSummary> getAppointmentHistory(Pageable pageable) {
        Authentication auth = getAuthentication();
        long id = Long.parseLong(auth.getName());
        Patient patient = patientRepository.findByUser_Id(id).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND)
        );

        return appointmentRepository
                .findByPatientId(patient.getId(), pageable).map(appointmentMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceSummary> getPatientInvoice(Pageable pageable) {
        Authentication auth = getAuthentication();
        long id = Long.parseLong(auth.getName());
        Patient patient = patientRepository.findByUser_Id(id).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND)
        );

        return invoiceRepository
                .findByPatientId(patient.getId(), pageable).map(invoiceMapper::toSummary);
    }


    @Transactional(readOnly = true)
    public PatientDetails getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        long id = Long.parseLong(auth.getName());
        Patient patient = patientRepository.findByUser_Id(id).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND)
        );


        return patientMapper.toDetails(patient);
    }

    @Transactional
    public PatientDetails updatePatient(long id, PatientUpdateRequest request) {
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND)
        );
        if (patient.getDeletedAt() != null) {
            throw new AppException(ErrorCode.PATIENT_NOT_FOUND);
        }

        if (request.firstName() != null && !request.firstName().isBlank())
            patient.setFirstName(request.firstName());

        if (request.lastName() != null && !request.lastName().isBlank())
            patient.setLastName(request.lastName());

        if (request.dateOfBirth() != null)
            patient.setDateOfBirth(request.dateOfBirth());

        if (request.gender() != null && !request.gender().isBlank()) {
            if(request.gender().equalsIgnoreCase(String.valueOf(Gender.MALE))
                    || request.gender().equalsIgnoreCase(String.valueOf(Gender.FEMALE))
                    || request.gender().equalsIgnoreCase(String.valueOf(Gender.OTHER))) {
                patient.setGender(Gender.valueOf(request.gender().toUpperCase()));
            }else {
                throw new AppException(ErrorCode.INVALID_GENDER);
            }
        }
        if (request.bloodType() != null && !request.bloodType().isBlank())
            patient.setBloodType(request.bloodType());

        if (request.phone() != null && !request.phone().isBlank())
            patient.setPhone(request.phone());

        if (request.email() != null && !request.email().isBlank())
            patient.setEmail(request.email());

        if (request.address() != null && !request.address().isBlank())
            patient.setAddress(request.address());

        if (request.emergencyContactName() != null && !request.emergencyContactName().isBlank())
            patient.setEmergencyContactName(request.emergencyContactName());

        if (request.emergencyContactPhone() != null && !request.emergencyContactPhone().isBlank())
            patient.setEmergencyContactPhone(request.emergencyContactPhone());
        auditService.log(AuditAction.PATIENT_UPDATED.name(), patient.getFirstName() + " " + patient.getLastName(), patient.getId());
        return patientMapper.toDetails(patientRepository.save(patient));
    }

    public void deletePatient(long id) {
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND));
        if (patient.getDeletedAt() != null) {
            throw new AppException(ErrorCode.PATIENT_NOT_FOUND);
        }
        validate(patient);
        patient.setDeletedAt(LocalDateTime.now());
        patientRepository.save(patient);
        auditService.log(AuditAction.PATIENT_DELETED.name(), patient.getFirstName() + " " + patient.getLastName(), patient.getId());
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private void validate(Patient patient) {
        Authentication auth = getAuthentication();
        long currentUserId = Long.parseLong(auth.getName());
        boolean isSelf = currentUserId == patient.getUser().getId();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isSelf && !isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }


}
