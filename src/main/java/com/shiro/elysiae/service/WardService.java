package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.wardsandbed.BedAddRequest;
import com.shiro.elysiae.dto.request.wardsandbed.WardCreateRequest;
import com.shiro.elysiae.dto.request.wardsandbed.WardSearchRequest;
import com.shiro.elysiae.dto.response.wardsandbeds.BedDetails;
import com.shiro.elysiae.dto.response.wardsandbeds.BedSummary;
import com.shiro.elysiae.dto.response.wardsandbeds.WardsDetails;
import com.shiro.elysiae.dto.response.wardsandbeds.WardsSummary;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.enums.AuditAction;
import com.shiro.elysiae.model.enums.BedStatus;
import com.shiro.elysiae.model.wardsbedsadmission.Bed;
import com.shiro.elysiae.model.wardsbedsadmission.Ward;
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
public class WardService {

    private final PatientRepository patientRepository;
    private final WardRepository wardRepository;
    private final BedRepository bedRepository;
    private final WardMapper wardMapper;
    private final BedMapper bedMapper;
    private final AdmissionRepository admissionRepository;
    private final DoctorRepository doctorRepository;
    private final AuditService auditService;

    public WardsDetails registerWard(WardCreateRequest request) {
        Ward ward = Ward.builder()
                .name(request.name())
                .type(request.type())
                .floor(request.floor())
                .build();
        return wardMapper.toDetailsResponse(wardRepository.save(ward));
    }

    public BedDetails addBed(BedAddRequest request) {
        Ward ward = wardRepository.findById(request.wardId()).orElseThrow(
                () -> new AppException(ErrorCode.WARD_NOT_FOUND)
        );
        if (ward.getDeletedAt() != null) {
            throw new AppException(ErrorCode.WARD_NOT_FOUND);
        }
        Bed bed = Bed.builder()
                .ward(ward)
                .bedNo(request.bedNo())
                .status(request.status())
                .build();
        return bedMapper.toDetailsResponse(bedRepository.save(bed));
    }

    @Transactional(readOnly = true)
    public BedDetails getBedDetails(long id) {
        Bed bed = bedRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.BED_NOT_FOUND)
        );
        if (bed.getDeletedAt() != null) {
            throw new AppException(ErrorCode.BED_NOT_FOUND);
        }
        return bedMapper.toDetailsResponse(bed);
    }
    @Transactional(readOnly = true)
    public WardsDetails getWardDetails(long id) {
        Ward ward = wardRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.WARD_NOT_FOUND)
        );
        if (ward.getDeletedAt() != null) {
            throw new AppException(ErrorCode.WARD_NOT_FOUND);
        }
        return wardMapper.toDetailsResponse(ward);
    }
    @Transactional(readOnly = true)
    public Page<WardsSummary> getAllWards(WardSearchRequest request
            , Pageable pageable) {
        return wardRepository.searchWards(
                request.name(),
                request.type(),
                request.floor(),
                pageable);
    }
    @Transactional(readOnly = true)
    public Page<BedSummary> getAllBeds(long id, BedStatus status,Pageable pageable) {
        Ward ward = wardRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.WARD_NOT_FOUND)
        );
        if (ward.getDeletedAt() != null) {
            throw new AppException(ErrorCode.WARD_NOT_FOUND);
        }
        return bedRepository.findByWardAndStatus(ward.getId(),status,pageable).map(bedMapper::toSummaryResponse);
    }

    public void deleteWard(long id) {
        Ward ward = wardRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.WARD_NOT_FOUND)
        );
        if (ward.getDeletedAt() != null) {
            throw new AppException(ErrorCode.WARD_NOT_FOUND);
        }
        ward.setDeletedAt(LocalDateTime.now());
        if (ward.getBeds() != null) {
            for (Bed bed : ward.getBeds()) {
                if (bed.getDeletedAt() == null) {
                    bed.setDeletedAt(LocalDateTime.now());
                }
            }
        }
        wardRepository.save(ward);
        auditService.log(AuditAction.WARD_DELETED.name(), ward.getName(), ward.getId());
    }

    public void deleteBed(long id) {
        Bed bed = bedRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.BED_NOT_FOUND)
        );
        if (bed.getDeletedAt() != null) {
            throw new AppException(ErrorCode.BED_NOT_FOUND);
        }
        bed.setDeletedAt(LocalDateTime.now());
        bedRepository.save(bed);
        auditService.log(AuditAction.BED_DELETED.name(), bed.getBedNo(), bed.getId());
    }


}
