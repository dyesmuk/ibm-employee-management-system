package com.ibm.ems.service.impl;

import com.ibm.ems.dto.department.DepartmentRequest;
import com.ibm.ems.dto.department.DepartmentResponse;
import com.ibm.ems.exception.BadRequestException;
import com.ibm.ems.exception.ConflictException;
import com.ibm.ems.exception.ResourceNotFoundException;
import com.ibm.ems.model.Department;
import com.ibm.ems.repository.DepartmentRepository;
import com.ibm.ems.repository.EmployeeRepository;
import com.ibm.ems.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Department business logic.
 *
 * DevSecOps fixes:
 *   - Typed exceptions replace raw RuntimeException (safe error messages)
 *   - Duplicate name check before save (business integrity)
 *   - Cannot delete department with active employees (data safety rule)
 *   - @Transactional(readOnly=true) on queries — performance + prevents
 *     accidental writes in read methods
 */
@Service
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository   employeeRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                  EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository   = employeeRepository;
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        return DepartmentResponse.from(findById(id));
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new ConflictException(
                "Department already exists with name = '" + request.getName() + "'");
        }
        Department dept = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return DepartmentResponse.from(departmentRepository.save(dept));
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department existing = findById(id);

        // Check unique name only if name is changing
        if (!existing.getName().equals(request.getName())
                && departmentRepository.existsByName(request.getName())) {
            throw new ConflictException(
                "Department already exists with name = '" + request.getName() + "'");
        }

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        return DepartmentResponse.from(departmentRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        findById(id); // confirm it exists
        if (employeeRepository.existsByDepartmentId(id)) {
            throw new BadRequestException(
                "Cannot delete department: it has employees assigned. " +
                "Reassign or terminate employees before deleting this department.");
        }
        departmentRepository.deleteById(id);
    }

    // ── Internal helper ───────────────────────────────────────────────────────
    private Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }
}
