package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.pharmacy.DispenseLogCreateRequest;
import com.shiro.elysiae.dto.request.pharmacy.MedicineCreateRequest;
import com.shiro.elysiae.dto.request.pharmacy.MedicineSearchRequest;
import com.shiro.elysiae.dto.request.pharmacy.MedicineUpdateRequest;
import com.shiro.elysiae.dto.response.pharmacy.DispenseLogDetails;
import com.shiro.elysiae.dto.response.pharmacy.DispenseLogSummary;
import com.shiro.elysiae.dto.response.pharmacy.MedicineDetails;
import com.shiro.elysiae.dto.response.pharmacy.MedicineSummary;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.User;
import com.shiro.elysiae.model.ehrnprescriptionsnvitals.Prescription;
import com.shiro.elysiae.model.enums.AuditAction;
import com.shiro.elysiae.model.pharmacy.DispenseLog;
import com.shiro.elysiae.model.pharmacy.Medicine;
import com.shiro.elysiae.repository.DispenseLogRepository;
import com.shiro.elysiae.repository.MedicineRepository;
import com.shiro.elysiae.repository.PrescriptionRepository;
import com.shiro.elysiae.repository.UserRepository;
import com.shiro.elysiae.util.DispenseLogMapper;
import com.shiro.elysiae.util.MedicineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacyService {

    private final DispenseLogRepository dispenseLogRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;
    private final AuditService auditService;
    private final DispenseLogMapper dispenseLogMapper;
    private final UserRepository userRepository;
    private final PrescriptionRepository prescriptionRepository;

    public MedicineDetails addMedicine(MedicineCreateRequest request) {
        Medicine medicine = Medicine.builder()
                .name(request.name())
                .genericName(request.genericName())
                .category(request.category())
                .stockQuantity(request.stockQuantity())
                .reorderLevel(request.reorderLevel())
                .unitPrice(request.unitPrice())
                .expiryDate(request.expiryDate())
                .build();
        if (medicineRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(request.name()))
            throw new AppException(ErrorCode.MEDICINE_ALREADY_EXISTS);
        auditService.log(String.valueOf(AuditAction.MEDICINE_ADDED), medicine.getName(), medicine.getId());
        return medicineMapper.toDetails(medicineRepository.save(medicine));
    }

    @Transactional(readOnly = true)
    public Page<MedicineSummary> searchMedicine(MedicineSearchRequest request, Pageable pageable) {
        return medicineRepository.search(
                request.name(),
                request.genericName(),
                request.category(),
                request.lowStock(),
                request.expiryBefore(),
                pageable).map(medicineMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public MedicineDetails getMedicineDetails(long id) {
        Medicine medicine = medicineRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.MEDICINE_NOT_FOUND)
        );
        MedicineDetails medicineDetails = medicineMapper.toDetails(medicine);
        if (medicine.getDeletedAt() != null) {
            throw new AppException(ErrorCode.MEDICINE_NOT_FOUND);
        }
        return medicineDetails;
    }

    public MedicineDetails updateMedicine(MedicineUpdateRequest request) {
        Medicine medicine = medicineRepository.findById(request.id()).orElseThrow(
                () -> new AppException(ErrorCode.MEDICINE_NOT_FOUND)
        );
        if (medicine.getDeletedAt() != null) {
            throw new AppException(ErrorCode.MEDICINE_NOT_FOUND);
        }
        if (request.name() != null) {
            medicine.setName(request.name());
        }
        if (request.genericName() != null) {
            medicine.setGenericName(request.genericName());
        }
        if (request.category() != null) {
            medicine.setCategory(request.category());
        }
        if (request.stockQuantity() != null) {
            medicine.setStockQuantity(request.stockQuantity());
        }
        if (request.reorderLevel() != null) {
            medicine.setReorderLevel(request.reorderLevel());
        }
        if (request.unitPrice() != null) {
            medicine.setUnitPrice(request.unitPrice());
        }
        if (request.expiryDate() != null) {
            medicine.setExpiryDate(request.expiryDate());
        }
        auditService.log(String.valueOf(AuditAction.MEDICINE_UPDATED), medicine.getName(), medicine.getId());
        return medicineMapper.toDetails(medicineRepository.save(medicine));
    }

    public MedicineDetails addStock(long id, Integer stockQuantity) {
        Medicine medicine = medicineRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.MEDICINE_NOT_FOUND)
        );
        if (medicine.getDeletedAt() != null) {
            throw new AppException(ErrorCode.MEDICINE_NOT_FOUND);
        }
        if (medicine.getStockQuantity() != null && stockQuantity != null) {
            medicine.setStockQuantity(medicine.getStockQuantity() + stockQuantity);
        }

        auditService.log(String.valueOf(AuditAction.MEDICINE_STOCK_UPDATED), medicine.getName(), medicine.getId());
        return medicineMapper.toDetails(medicineRepository.save(medicine));
    }

    public Page<DispenseLogSummary> getDispenseLogs(long id,Pageable pageable) {
        return dispenseLogRepository.findByPrescriptionId(id,pageable)
                .map(dispenseLogMapper::toSummary);
    }

    public Page<DispenseLogSummary> getDispenseLogs(Pageable pageable) {
        return dispenseLogRepository.findAll(pageable)
                .map(dispenseLogMapper::toSummary);
    }

    public DispenseLogDetails dispenseMedicine(DispenseLogCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        long currentUserId = Long.parseLong(auth.getName());
        User user = userRepository.findById(currentUserId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        Medicine medicine = medicineRepository.findById(request.medicineId()).orElseThrow(
                () -> new AppException(ErrorCode.MEDICINE_NOT_FOUND)
        );
        if (medicine.getDeletedAt() != null) {
            throw new AppException(ErrorCode.MEDICINE_NOT_FOUND);
        }
        if(request.quantity() > medicine.getStockQuantity()) {
            throw new AppException(ErrorCode.MEDICINE_INSUFFICIENT_STOCK);
        }

        if(medicine.getExpiryDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.EXPIRED_MEDICINE);
        }
        Prescription prescription = prescriptionRepository.findById(request.prescriptionId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));
        if(prescription.getDispensed()) {
            throw new AppException(ErrorCode.PRESCRIPTION_ALREADY_DISPENSED);
        }

        DispenseLog dispenseLog = DispenseLog.builder()
                .prescription(prescription)
                .medicine(medicine)
                .quantity(request.quantity())
                .dispensedBy(user)
                .dispensedAt(LocalDateTime.now()).build();
        DispenseLog saved = dispenseLogRepository.save(dispenseLog);

        prescription.setDispensed(true);
        prescriptionRepository.save(prescription);

        medicine.setStockQuantity(medicine.getStockQuantity() - saved.getQuantity());
        medicineRepository.save(medicine);

        auditService.log(String.valueOf(AuditAction.MEDICINE_DISPENSED), medicine.getName(), medicine.getId());
        return dispenseLogMapper.toDetails(saved);
    }


}
