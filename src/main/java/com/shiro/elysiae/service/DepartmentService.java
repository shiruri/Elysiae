package com.shiro.elysiae.service;


import com.shiro.elysiae.dto.request.department.DepartmentCreateRequest;
import com.shiro.elysiae.dto.request.department.DepartmentUpdateRequest;
import com.shiro.elysiae.dto.response.department.DepartmentDetails;
import com.shiro.elysiae.dto.response.department.DepartmentSummary;
import com.shiro.elysiae.dto.response.doctor.DoctorSummary;
import com.shiro.elysiae.exception.AppException;
import com.shiro.elysiae.exception.ErrorCode;
import com.shiro.elysiae.model.doctorsndepartment.Department;
import com.shiro.elysiae.model.doctorsndepartment.Doctor;
import com.shiro.elysiae.model.patient.Patient;
import com.shiro.elysiae.repository.DepartmentRepository;
import com.shiro.elysiae.repository.DoctorRepository;
import com.shiro.elysiae.util.DepartmentMapper;
import com.shiro.elysiae.util.DoctorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public Page<DepartmentSummary> searchDepartments(String keyword, Pageable pageable) {
        return departmentRepository.searchDepartments(keyword,pageable).map(departmentMapper::toDepartmentSummary);
    }

    @Transactional(readOnly = true)
    public Page<DepartmentSummary> getDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable).map(departmentMapper::toDepartmentSummary);
    }

    @Transactional(readOnly = true)
    public DepartmentDetails getDepartmentById(long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        List<DoctorSummary> doctors = doctorRepository.searchByDepartmentId(id).stream().map(doctorMapper::toSummary).toList();
        return toDetails(department,doctors);

    }


    @Transactional
    public DepartmentDetails registerDepartment(DepartmentCreateRequest request) {
        Department department = Department.builder()
                .name(request.name())
                .floor(request.floor())
                .build();
        departmentRepository.save(department);
        return departmentMapper.toDepartmentDetails(department);
    }

    @Transactional
    public DepartmentDetails updateDepartment(DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(request.id())
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));

        if(!request.name().isBlank()) {
            department.setName(request.name());
        }
        if(!request.floor().isBlank()) {
            department.setFloor(request.floor());
        }
        return departmentMapper.toDepartmentDetails(departmentRepository.save(department));
    }

    @Transactional
    public void deleteDepartment(long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        validate();
        departmentRepository.delete(department);
    }



    private void validate() {
        Authentication auth = getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if(!isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }



    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }



    @Transactional
    private DepartmentDetails toDetails(Department department, List<DoctorSummary> doctors) {

        return new DepartmentDetails(
                department.getId(),
                department.getName(),
                department.getFloor(),
                doctors);

    }



}
