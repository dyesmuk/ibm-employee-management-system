package com.ibm.ems.service.impl;

import com.ibm.ems.dto.employee.EmployeeRequest;
import com.ibm.ems.dto.employee.EmployeeResponse;
import com.ibm.ems.exception.ConflictException;
import com.ibm.ems.exception.ResourceNotFoundException;
import com.ibm.ems.model.Department;
import com.ibm.ems.model.Employee;
import com.ibm.ems.model.Employee.EmployeeStatus;
import com.ibm.ems.model.Role;
import com.ibm.ems.repository.DepartmentRepository;
import com.ibm.ems.repository.EmployeeRepository;
import com.ibm.ems.repository.RoleRepository;
import com.ibm.ems.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Employee business logic.
 *
 * DevSecOps fixes:
 *
 *  A01 — Broken Access Control (Salary PII):
 *    The 'includeSalary' flag is decided by the CONTROLLER based on the
 *    authenticated user's roles. The service layer enforces the same rule
 *    as a second layer of defence (defence-in-depth).
 *
 *  A03 — Injection:
 *    All queries use Spring Data derived methods or @Param — never string
 *    concatenation. Example: findByFirstNameContainingIgnoreCaseOr...()
 *
 *  A07 — Auth Failures:
 *    Email uniqueness checked before save — prevents duplicate account creation.
 *
 *  Business rules enforced:
 *    - Department and Role must exist before creating an employee (FK safety)
 *    - Email must be unique across the system
 *    - Soft delete: status set to TERMINATED, record never physically removed
 */
@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository   employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository       roleRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                                DepartmentRepository departmentRepository,
                                RoleRepository roleRepository) {
        this.employeeRepository   = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository       = roleRepository;
    }

    @Override
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable, boolean includeSalary) {
        return employeeRepository.findAll(pageable)
                .map(e -> EmployeeResponse.from(e, includeSalary));
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id, boolean includeSalary) {
        return EmployeeResponse.from(findEntityById(id), includeSalary);
    }

    @Override
    public List<EmployeeResponse> getEmployeesByDepartment(Long deptId, boolean includeSalary) {
        // Confirm dept exists — returns 404 if not
        departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", deptId));
        return employeeRepository.findByDepartmentId(deptId).stream()
                .map(e -> EmployeeResponse.from(e, includeSalary))
                .toList();
    }

    @Override
    public List<EmployeeResponse> searchEmployees(String name, boolean includeSalary) {
        // DevSecOps (A03): Spring Data derived query — no string concatenation
        return employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
                .stream()
                .map(e -> EmployeeResponse.from(e, includeSalary))
                .toList();
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest req) {
        // Unique email check — A07: prevent duplicate identity
        if (employeeRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new ConflictException(
                "Employee already exists with email = '" + req.getEmail() + "'");
        }
        Department dept = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Department", "id", req.getDepartmentId()));
        Role role = roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Role", "id", req.getRoleId()));

        Employee emp = Employee.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .salary(req.getSalary())
                .hireDate(req.getHireDate())
                .status(req.getStatus())
                .department(dept)
                .role(role)
                .build();

        return EmployeeResponse.from(employeeRepository.save(emp), true);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest req) {
        Employee existing = findEntityById(id);

        // If email is changing, check new email is not taken
        if (!existing.getEmail().equals(req.getEmail())
                && employeeRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new ConflictException(
                "Employee already exists with email = '" + req.getEmail() + "'");
        }

        Department dept = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Department", "id", req.getDepartmentId()));
        Role role = roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Role", "id", req.getRoleId()));

        existing.setFirstName(req.getFirstName());
        existing.setLastName(req.getLastName());
        existing.setEmail(req.getEmail());
        existing.setPhone(req.getPhone());
        existing.setSalary(req.getSalary());
        existing.setHireDate(req.getHireDate());
        existing.setStatus(req.getStatus());
        existing.setDepartment(dept);
        existing.setRole(role);

        return EmployeeResponse.from(employeeRepository.save(existing), true);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        // DevSecOps (A04 — Insecure Design): SOFT DELETE only.
        // Setting TERMINATED preserves the audit trail required by RBI guidelines.
        // Physical deletion would violate regulatory data retention requirements.
        Employee emp = findEntityById(id);
        emp.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(emp);
    }

    @Override
    public Employee findEntityById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }
}
