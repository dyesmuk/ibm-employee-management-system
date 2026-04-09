package com.ibm.ems.service;

import com.ibm.ems.dto.DepartmentRequest;
import com.ibm.ems.dto.DepartmentResponse;
import com.ibm.ems.exception.DuplicateResourceException;
import com.ibm.ems.exception.ResourceNotFoundException;
import com.ibm.ems.model.Department;
import com.ibm.ems.repository.DepartmentRepository;
import com.ibm.ems.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository   employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository   = employeeRepository;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public DepartmentResponse create(DepartmentRequest request) {
        log.debug("Creating department: {}", request.getName());
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }
        Department dept = new Department(request.getName(), request.getLocation());
        Department saved = departmentRepository.save(dept);
        log.info("Created department id={}, name={}", saved.getId(), saved.getName());
        return DepartmentResponse.from(saved);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        Department dept = departmentRepository.findByIdWithEmployees(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return DepartmentResponse.from(dept);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findByName(String name) {
        Department dept = departmentRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "name", name));
        return DepartmentResponse.from(dept);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getHeadcountReport() {
        Map<String, Long> report = new LinkedHashMap<>();
        departmentRepository.getDepartmentHeadcount()
                .forEach(row -> report.put((String) row[0], (Long) row[1]));
        return report;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        if (!dept.getName().equalsIgnoreCase(request.getName()) &&
                departmentRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }

        dept.setName(request.getName());
        dept.setLocation(request.getLocation());
        Department updated = departmentRepository.save(dept);
        log.info("Updated department id={}", updated.getId());
        return DepartmentResponse.from(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department", "id", id);
        }
        long empCount = employeeRepository.countByDepartmentId(id);
        if (empCount > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete department with " + empCount +
                    " employee(s). Reassign or remove employees first.");
        }
        departmentRepository.deleteById(id);
        log.info("Deleted department id={}", id);
    }
}
