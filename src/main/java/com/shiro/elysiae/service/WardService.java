package com.shiro.elysiae.service;

import com.shiro.elysiae.dto.request.wardsandbed.BedAddRequest;
import com.shiro.elysiae.dto.request.wardsandbed.BedAdmitPatientRequest;
import com.shiro.elysiae.dto.request.wardsandbed.WardCreateRequest;
import com.shiro.elysiae.dto.request.wardsandbed.WardSearchRequest;
import com.shiro.elysiae.dto.response.wardsandbeds.BedDetails;
import com.shiro.elysiae.dto.response.wardsandbeds.BedSummary;
import com.shiro.elysiae.dto.response.wardsandbeds.WardsDetails;
import com.shiro.elysiae.dto.response.wardsandbeds.WardsSummary;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.appointments.Appointment;
import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.enums.AdmissionStatus;
import com.shiro.elysiae.model.enums.BedStatus;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.model.wardsbedsadmission.Admission;
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
import java.util.Optional;

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
        Bed bed = Bed.builder()
                .ward(ward)
                .bedNo(request.bedNo())
                .status(request.status())
                .build();
        return bedMapper.toDetailsResponse(bedRepository.save(bed));
    }

    public BedDetails getBedDetails(long id) {
        return bedMapper.toDetailsResponse(bedRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.BED_NOT_FOUND)
        ));
    }
    public WardsDetails getWardDetails(long id) {
        return wardMapper.toDetailsResponse(wardRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.BED_NOT_FOUND)
        ));
    }

    public Page<WardsSummary> getAllWards(WardSearchRequest request
            , Pageable pageable) {
        return wardRepository.searchWards(
                request.name(),
                request.type(),
                request.floor(),
                pageable);
    }

    public Page<BedSummary> getAllBeds(long id, BedStatus status,Pageable pageable) {
        Ward ward = wardRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.WARD_NOT_FOUND)
        );
        return bedRepository.findByWardAndStatus(ward.getId(),status,pageable).map(bedMapper::toSummaryResponse);
    }



}
