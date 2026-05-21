package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.ehr.MedicalRecordCreateRequest;
import com.shiro.elysiae.dto.request.ehr.MedicalRecordUpdateRequest;
import com.shiro.elysiae.dto.request.ehr.PrescriptionCreateRequest;
import com.shiro.elysiae.dto.response.medical.MedicalRecordDetails;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.appointments.Appointment;
import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.ehrnprescriptionsnvitals.MedicalRecord;
import com.shiro.elysiae.model.ehrnprescriptionsnvitals.Prescription;
import com.shiro.elysiae.model.enums.AdmissionStatus;
import com.shiro.elysiae.model.enums.AuditAction;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.model.wardsbedsadmission.Admission;
import com.shiro.elysiae.repository.*;
import com.shiro.elysiae.util.MedicalRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RecordsService {

    private final PatientRepository patientRepository;
    private final AdmissionRepository admissionRepository;
    private final DoctorRepository doctorRepository;
    private final AuditService auditService;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordMapper medicalRecordMapper;
    private final PrescriptionRepository prescriptionRepository;

    public MedicalRecordDetails createMedicalRecord(MedicalRecordCreateRequest request) {
        if (request.appointmentId() == null && request.admissionId() == null)
            throw new AppException(ErrorCode.RECORD_SOURCE_REQUIRED);

        Patient patient = patientRepository.findById(request.patientId()).orElseThrow(
                () -> new AppException(ErrorCode.PATIENT_NOT_FOUND)
        );

        Doctor doctor = doctorRepository.findById(request.doctorId()).orElseThrow(
                () -> new AppException(ErrorCode.DOCTOR_NOT_FOUND)
        );

        Appointment appointment = null;
        if (request.appointmentId() != null) {
            appointment = appointmentRepository.findById(request.appointmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
        }

        Admission admission = null;
        String diagnosis = request.diagnosis();
        if (request.admissionId() != null) {
            admission = admissionRepository.findById(request.admissionId())
                    .orElseThrow(() -> new AppException(ErrorCode.ADMISSION_NOT_FOUND));
            if (diagnosis == null && admission.getStatus() == AdmissionStatus.DISCHARGED) {
                diagnosis = admission.getDiagnosis();
            }
        }

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .admission(admission)
                .diagnosis(diagnosis)
                .notes(request.notes())
                .build();
        auditService.log(String.valueOf(AuditAction.MEDICAL_RECORD_CREATED),record.getPatient().getFirstName() + " "
        + record.getPatient().getLastName(),record.getId());
        return medicalRecordMapper.toDetails(medicalRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public MedicalRecordDetails getMedicalRecord(long id) {
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
        MedicalRecord record = medicalRecordRepository.findByIdWithPrescriptions(id).orElseThrow(
                () -> new AppException(ErrorCode.MEDICAL_RECORD_NOT_FOUND));
        if(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            validate(record.getPatient().getUser().getId());
        }
        return medicalRecordMapper.toDetails(record);
    }

    public MedicalRecordDetails updateMedicalRecord(MedicalRecordUpdateRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(request.recordId())
                .orElseThrow(() -> new AppException(ErrorCode.MEDICAL_RECORD_NOT_FOUND));

        validate(record.getDoctor().getUser().getId());

        if (request.diagnosis() != null && !request.diagnosis().isBlank()) {
            record.setDiagnosis(request.diagnosis());
        }
        if (request.notes() != null && !request.notes().isBlank()) {
            record.setNotes(request.notes());
        }

        MedicalRecord saved = medicalRecordRepository.save(record);
        auditService.log(String.valueOf(AuditAction.MEDICAL_RECORD_UPDATED),record.getPatient().getFirstName() + " "
                + record.getPatient().getLastName(),record.getId());
        return medicalRecordMapper.toDetails(
                medicalRecordRepository.findByIdWithPrescriptions(saved.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.MEDICAL_RECORD_NOT_FOUND))
        );
    }

    public MedicalRecordDetails addPrescription(PrescriptionCreateRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(request.recordId()).orElseThrow(
                () -> new AppException(ErrorCode.MEDICAL_RECORD_NOT_FOUND)
        );
        validate(record.getDoctor().getUser().getId());

        Prescription prescription = Prescription.builder()
                .medicalRecord(record)
                .medicineName(request.medicineName())
                .dosage(request.dosage())
                .frequency(request.frequency())
                .durationDays(request.durationDays())
                .build();
        prescriptionRepository.save(prescription);
        auditService.log(String.valueOf(AuditAction.PRESCRIPTION_ADDED),record.getPatient().getFirstName() + " "
                + record.getPatient().getLastName(),record.getId());
        return medicalRecordMapper.toDetails(medicalRecordRepository.findByIdWithPrescriptions(record.getId()).get());
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
