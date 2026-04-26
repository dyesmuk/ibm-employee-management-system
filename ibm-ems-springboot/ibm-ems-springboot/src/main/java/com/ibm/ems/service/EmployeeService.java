package com.ibm.ems.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ibm.ems.dto.employee.EmployeeRequest;
import com.ibm.ems.dto.employee.EmployeeResponse;
import com.ibm.ems.model.Employee;

import java.util.List;

public interface EmployeeService {
    Page<EmployeeResponse> getAllEmployees(Pageable pageable, boolean includeSalary);
    EmployeeResponse       getEmployeeById(String id, boolean includeSalary);
    List<EmployeeResponse> getEmployeesByDepartment(String deptId, boolean includeSalary);
    List<EmployeeResponse> searchEmployees(String name, boolean includeSalary);
    EmployeeResponse       createEmployee(EmployeeRequest request);
    EmployeeResponse       updateEmployee(String id, EmployeeRequest request);
    void                   deleteEmployee(String id);
    Employee               findEntityById(String id);
}
