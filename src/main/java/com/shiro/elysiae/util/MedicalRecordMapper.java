package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.medical.MedicalRecordSummary;
import com.shiro.elysiae.model.ehrnprescriptionsnvitals.MedicalRecord;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {

    MedicalRecordSummary toRecordSummary(MedicalRecord medicalRecord);
}
