package com.ibm.ems.service;

import java.util.List;

import com.ibm.ems.dto.employee.EmployeeResponse;
import com.ibm.ems.dto.project.AssignEmployeeRequest;
import com.ibm.ems.dto.project.ProjectRequest;
import com.ibm.ems.dto.project.ProjectResponse;
import com.ibm.ems.model.ProjectStatus;

public interface ProjectService {
	List<ProjectResponse> getAllProjects(ProjectStatus status);

	ProjectResponse getProjectById(String id);

	ProjectResponse createProject(ProjectRequest request);

	ProjectResponse updateProject(String id, ProjectRequest request);

	void deleteProject(String id);

	void assignEmployee(String projectId, AssignEmployeeRequest req);

	void removeEmployee(String projectId, String employeeId);

	List<EmployeeResponse> getProjectEmployees(String projectId);

	List<ProjectResponse> getEmployeeProjects(String employeeId);
}
