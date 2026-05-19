package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.department.DepartmentDetails;
import com.shiro.elysiae.dto.response.department.DepartmentSummary;
import com.shiro.elysiae.model.doctorsndepartment.Department;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentSummary toDepartmentSummary(Department department);

    DepartmentDetails toDepartmentDetails(Department department);
}
