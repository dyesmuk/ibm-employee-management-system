package com.ibm.ems.repository;

import com.ibm.ems.model.EmployeeProject;
import com.ibm.ems.model.EmployeeProject.EmployeeProjectId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeProjectRepository extends JpaRepository<EmployeeProject, EmployeeProjectId> {

    // Get all employees in a project
    List<EmployeeProject> findByProjectId(Long projectId);

    // Get all projects of an employee
    List<EmployeeProject> findByEmployeeId(Long employeeId);

    // Check assignment
    boolean existsByEmployeeIdAndProjectId(Long employeeId, Long projectId);
}