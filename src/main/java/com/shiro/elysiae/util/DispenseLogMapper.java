package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.pharmacy.DispenseLogDetails;
import com.shiro.elysiae.dto.response.pharmacy.DispenseLogSummary;
import com.shiro.elysiae.model.pharmacy.DispenseLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DispenseLogMapper {

    @Mapping(target = "medicineName", source = "medicine.name")
    @Mapping(target = "dispensedBy",  source = "dispensedBy.username")
    DispenseLogSummary toSummary(DispenseLog dispenseLog);

    @Mapping(target = "prescriptionId",      source = "prescription.id")
    @Mapping(target = "medicineName",        source = "prescription.medicineName")
    @Mapping(target = "dosage",              source = "prescription.dosage")
    @Mapping(target = "frequency",           source = "prescription.frequency")
    @Mapping(target = "durationDays",        source = "prescription.durationDays")
    @Mapping(target = "medicineId",          source = "medicine.id")
    @Mapping(target = "medicineGenericName", source = "medicine.genericName")
    @Mapping(target = "unitPrice",           source = "medicine.unitPrice")
    @Mapping(target = "quantityDispensed",   source = "quantity")
    @Mapping(target = "dispensedBy",         source = "dispensedBy.username")
    DispenseLogDetails toDetails(DispenseLog dispenseLog);
}
