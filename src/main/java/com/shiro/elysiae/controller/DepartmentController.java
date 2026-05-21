package com.shiro.elysiae.controller;

import com.shiro.elysiae.dto.request.department.DepartmentCreateRequest;
import com.shiro.elysiae.dto.request.department.DepartmentUpdateRequest;
import com.shiro.elysiae.dto.response.department.DepartmentDetails;
import com.shiro.elysiae.dto.response.department.DepartmentSummary;
import com.shiro.elysiae.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/department")
public class DepartmentController {

    private  final DepartmentService departmentService;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST','PATIENT','NURSE')")
    @GetMapping("/search")
    public ResponseEntity<Page<DepartmentSummary>> searchDepartments(@RequestParam String keyword
            , Pageable pageable) {
        return ResponseEntity.ok().body(departmentService.searchDepartments(keyword, pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST','PATIENT','NURSE')")
    @GetMapping()
    public ResponseEntity<Page<DepartmentSummary>> getDepartments(Pageable pageable) {
        return ResponseEntity.ok().body(departmentService.getDepartments(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<DepartmentDetails> registerDepartment(@Valid @RequestBody DepartmentCreateRequest request) {
        return ResponseEntity.ok().body(departmentService.registerDepartment(request));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDetails> getDepartmentById(@PathVariable long id) {
        return ResponseEntity.ok().body(departmentService.getDepartmentById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping()
    public ResponseEntity<DepartmentDetails> updateDepartment(@Valid @RequestBody DepartmentUpdateRequest request) {
        return ResponseEntity.ok().body(departmentService.updateDepartment(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDepartment(@PathVariable long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok().body("Deleted Successfully!");
    }
}
