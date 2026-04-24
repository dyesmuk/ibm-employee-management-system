package com.ibm.ems.repository;

import com.ibm.ems.model.Employee;
import com.ibm.ems.model.Employee.EmployeeStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find by email (login use-case)
    Optional<Employee> findByEmail(String email);

    // Department filter
    List<Employee> findByDepartmentId(Long departmentId);

    // Role filter
    List<Employee> findByRoleId(Long roleId);

    // Status filter
    List<Employee> findByStatus(EmployeeStatus status);

    // Combined filters (powerful)
    Page<Employee> findByDepartmentIdAndStatus(Long deptId, EmployeeStatus status, Pageable pageable);

    // Search by name (case insensitive)
    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    // Exists checks (used in service layer)
    boolean existsByDepartmentId(Long departmentId);

    boolean existsByRoleId(Long roleId);
}