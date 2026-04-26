package com.ibm.ems.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

import java.time.LocalDateTime;
import java.util.List;

@Service
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
                .map(e -> toResponse(e, includeSalary));
    }

    @Override
    public EmployeeResponse getEmployeeById(String id, boolean includeSalary) {
        return toResponse(findEntityById(id), includeSalary);
    }

    @Override
    public List<EmployeeResponse> getEmployeesByDepartment(String deptId, boolean includeSalary) {
        departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", deptId));
        return employeeRepository.findByDepartmentId(deptId).stream()
                .map(e -> toResponse(e, includeSalary))
                .toList();
    }

    @Override
    public List<EmployeeResponse> searchEmployees(String name, boolean includeSalary) {
        return employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
                .stream()
                .map(e -> toResponse(e, includeSalary))
                .toList();
    }

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest req) {
        if (employeeRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new ConflictException(
                "Employee already exists with email = '" + req.getEmail() + "'");
        }
        departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Department", "id", req.getDepartmentId()));
        roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Role", "id", req.getRoleId()));

        Employee emp = new Employee();
        emp.setFirstName(req.getFirstName());
        emp.setLastName(req.getLastName());
        emp.setEmail(req.getEmail());
        emp.setPhone(req.getPhone());
        emp.setSalary(req.getSalary());
        emp.setHireDate(req.getHireDate());
        emp.setStatus(req.getStatus());
        emp.setDepartmentId(req.getDepartmentId());
        emp.setRoleId(req.getRoleId());
        emp.setCreatedAt(LocalDateTime.now());
        emp.setUpdatedAt(LocalDateTime.now());

        return toResponse(employeeRepository.save(emp), true);
    }

    @Override
    public EmployeeResponse updateEmployee(String id, EmployeeRequest req) {
        Employee existing = findEntityById(id);
        if (!existing.getEmail().equals(req.getEmail())
                && employeeRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new ConflictException(
                "Employee already exists with email = '" + req.getEmail() + "'");
        }
        departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Department", "id", req.getDepartmentId()));
        roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Role", "id", req.getRoleId()));

        existing.setFirstName(req.getFirstName());
        existing.setLastName(req.getLastName());
        existing.setEmail(req.getEmail());
        existing.setPhone(req.getPhone());
        existing.setSalary(req.getSalary());
        existing.setHireDate(req.getHireDate());
        existing.setStatus(req.getStatus());
        existing.setDepartmentId(req.getDepartmentId());
        existing.setRoleId(req.getRoleId());
        existing.setUpdatedAt(LocalDateTime.now());

        return toResponse(employeeRepository.save(existing), true);
    }

    @Override
    public void deleteEmployee(String id) {
        Employee emp = findEntityById(id);
        emp.setStatus(EmployeeStatus.TERMINATED);
        emp.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(emp);
    }

    @Override
    public Employee findEntityById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    // ── Helper: resolve references and build response ─────────────────────────

    private EmployeeResponse toResponse(Employee emp, boolean includeSalary) {
        Department dept = emp.getDepartmentId() != null
                ? departmentRepository.findById(emp.getDepartmentId()).orElse(null) : null;
        Role role = emp.getRoleId() != null
                ? roleRepository.findById(emp.getRoleId()).orElse(null) : null;
        return EmployeeResponse.from(emp, includeSalary, dept, role);
    }
}
