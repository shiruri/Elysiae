package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.pharmacy.MedicineDetails;
import com.shiro.elysiae.dto.response.pharmacy.MedicineSummary;
import com.shiro.elysiae.model.pharmacy.Medicine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicineMapper {

    @Mapping(target = "lowStock", expression = "java(medicine.getStockQuantity() <= medicine.getReorderLevel())")
    MedicineSummary toSummary(Medicine medicine);

    @Mapping(target = "lowStock", expression = "java(medicine.getStockQuantity() <= medicine.getReorderLevel())")
    MedicineDetails toDetails(Medicine medicine);
}