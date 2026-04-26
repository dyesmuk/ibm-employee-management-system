package com.ibm.ems.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ibm.ems.model.EmployeeProject;

import java.util.List;
import java.util.Optional;

public interface EmployeeProjectRepository extends MongoRepository<EmployeeProject, String> {

    List<EmployeeProject> findByProjectId(String projectId);

    List<EmployeeProject> findByEmployeeId(String employeeId);

    boolean existsByEmployeeIdAndProjectId(String employeeId, String projectId);

    Optional<EmployeeProject> findByEmployeeIdAndProjectId(String employeeId, String projectId);
}
