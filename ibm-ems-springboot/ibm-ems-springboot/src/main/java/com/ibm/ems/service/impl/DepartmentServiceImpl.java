package com.ibm.ems.service.impl;

import org.springframework.stereotype.Service;

import com.ibm.ems.dto.department.DepartmentRequest;
import com.ibm.ems.dto.department.DepartmentResponse;
import com.ibm.ems.exception.BadRequestException;
import com.ibm.ems.exception.ConflictException;
import com.ibm.ems.exception.ResourceNotFoundException;
import com.ibm.ems.model.Department;
import com.ibm.ems.repository.DepartmentRepository;
import com.ibm.ems.repository.EmployeeRepository;
import com.ibm.ems.service.DepartmentService;

import java.util.List;

@Service
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
    public DepartmentResponse getDepartmentById(String id) {
        return DepartmentResponse.from(findById(id));
    }

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new ConflictException(
                "Department already exists with name = '" + request.getName() + "'");
        }
        Department dept = new Department();
        dept.setName(request.getName());
        dept.setDescription(request.getDescription());
        return DepartmentResponse.from(departmentRepository.save(dept));
    }

    @Override
    public DepartmentResponse updateDepartment(String id, DepartmentRequest request) {
        Department existing = findById(id);
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
    public void deleteDepartment(String id) {
        findById(id);
        if (employeeRepository.existsByDepartmentId(id)) {
            throw new BadRequestException(
                "Cannot delete department: it has employees assigned. " +
                "Reassign or terminate employees before deleting this department.");
        }
        departmentRepository.deleteById(id);
    }

    private Department findById(String id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }
}
