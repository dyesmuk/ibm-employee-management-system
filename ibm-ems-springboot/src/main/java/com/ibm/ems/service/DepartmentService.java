package com.ibm.ems.service;

import com.ibm.ems.dto.department.DepartmentRequest;
import com.ibm.ems.dto.department.DepartmentResponse;
import java.util.List;

public interface DepartmentService {
    List<DepartmentResponse> getAllDepartments();
    DepartmentResponse       getDepartmentById(Long id);
    DepartmentResponse       createDepartment(DepartmentRequest request);
    DepartmentResponse       updateDepartment(Long id, DepartmentRequest request);
    void                     deleteDepartment(Long id);
}
