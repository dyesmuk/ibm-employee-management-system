//package com.ibm.ems.service;
//
//import com.ibm.ems.dto.DepartmentRequest;
//import com.ibm.ems.dto.DepartmentResponse;
//import com.ibm.ems.exception.DuplicateResourceException;
//import com.ibm.ems.exception.ResourceNotFoundException;
//import com.ibm.ems.model.Department;
//import com.ibm.ems.repository.DepartmentRepository;
//import com.ibm.ems.repository.EmployeeRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class DepartmentService {
//
//    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);
//
//    private final DepartmentRepository departmentRepository;
//    private final EmployeeRepository employeeRepository;
//
//    public DepartmentService(DepartmentRepository departmentRepository,
//                             EmployeeRepository employeeRepository) {
//        this.departmentRepository = departmentRepository;
//        this.employeeRepository = employeeRepository;
//    }
//
//
//    public DepartmentResponse create(DepartmentRequest request) {
//
//        log.debug("Creating department: {}", request.getName());
//
//        if (departmentRepository.existsByName(request.getName())) {
//            throw new DuplicateResourceException("Department", "name", request.getName());
//        }
//
//        Department dept = new Department();
//        dept.setName(request.getName());
//        dept.setLocation(request.getLocation());
//
//        Department saved = departmentRepository.save(dept);
//
//        log.info("Created department id={}, name={}", saved.getId(), saved.getName());
//
//        return DepartmentResponse.from(saved, 0);
//    }
//
//
//    public List<DepartmentResponse> findAll() {
//
//        return departmentRepository.findAll()
//                .stream()
//                .map(dept -> {
//                    long count = employeeRepository.countByDepartmentId(dept.getId());
//                    return DepartmentResponse.from(dept, count);
//                })
//                .collect(Collectors.toList());
//    }
//
//
//    public DepartmentResponse findById(String id) {
//
//        Department dept = departmentRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
//
//        long count = employeeRepository.countByDepartmentId(id);
//
//        return DepartmentResponse.from(dept, count);
//    }
//
//
//    public DepartmentResponse findByName(String name) {
//
//        Department dept = departmentRepository.findByName(name)
//                .orElseThrow(() -> new ResourceNotFoundException("Department", "name", name));
//
//        long count = employeeRepository.countByDepartmentId(dept.getId());
//
//        return DepartmentResponse.from(dept, count);
//    }
//
//
//    public Map<String, Long> getHeadcountReport() {
//
//        Map<String, Long> report = new LinkedHashMap<>();
//
//        departmentRepository.findAll().forEach(dept -> {
//            long count = employeeRepository.countByDepartmentId(dept.getId());
//            report.put(dept.getName(), count);
//        });
//
//        return report;
//    }
//
//    // ── UPDATE ───────────────────────────────────────────────────────────────
//
//    public DepartmentResponse update(String id, DepartmentRequest request) {
//
//        Department dept = departmentRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
//
//        // Duplicate check (Mongo style)
//        Department existing = departmentRepository.findByName(request.getName()).orElse(null);
//
//        if (existing != null && !existing.getId().equals(id)) {
//            throw new DuplicateResourceException("Department", "name", request.getName());
//        }
//
//        dept.setName(request.getName());
//        dept.setLocation(request.getLocation());
//
//        Department updated = departmentRepository.save(dept);
//
//        log.info("Updated department id={}", updated.getId());
//
//        long count = employeeRepository.countByDepartmentId(id);
//
//        return DepartmentResponse.from(updated, count);
//    }
//
//    // ── DELETE ───────────────────────────────────────────────────────────────
//
//    public void delete(String id) {
//
//        if (!departmentRepository.existsById(id)) {
//            throw new ResourceNotFoundException("Department", "id", id);
//        }
//
//        long empCount = employeeRepository.countByDepartmentId(id);
//
//        if (empCount > 0) {
//            throw new IllegalArgumentException(
//                    "Cannot delete department with " + empCount +
//                    " employee(s). Reassign or remove employees first.");
//        }
//
//        departmentRepository.deleteById(id);
//
//        log.info("Deleted department id={}", id);
//    }
//}