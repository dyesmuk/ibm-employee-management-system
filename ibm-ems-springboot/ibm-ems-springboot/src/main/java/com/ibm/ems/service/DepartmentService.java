package com.ibm.ems.service;

import java.util.List;

import com.ibm.ems.dto.department.DepartmentRequest;
import com.ibm.ems.dto.department.DepartmentResponse;

public interface DepartmentService {
    List<DepartmentResponse> getAllDepartments();
    DepartmentResponse       getDepartmentById(String id);
    DepartmentResponse       createDepartment(DepartmentRequest request);
    DepartmentResponse       updateDepartment(String id, DepartmentRequest request);
    void                     deleteDepartment(String id);
}
