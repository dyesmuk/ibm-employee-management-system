package com.ibm.ems.service;

import com.ibm.ems.dto.employee.EmployeeResponse;
import com.ibm.ems.dto.project.AssignEmployeeRequest;
import com.ibm.ems.dto.project.ProjectRequest;
import com.ibm.ems.dto.project.ProjectResponse;
import com.ibm.ems.model.Project.ProjectStatus;
import java.util.List;

public interface ProjectService {
    List<ProjectResponse>    getAllProjects(ProjectStatus status);
    ProjectResponse          getProjectById(Long id);
    ProjectResponse          createProject(ProjectRequest request);
    ProjectResponse          updateProject(Long id, ProjectRequest request);
    void                     deleteProject(Long id);
    void                     assignEmployee(Long projectId, AssignEmployeeRequest req);
    void                     removeEmployee(Long projectId, Long employeeId);
    List<EmployeeResponse>   getProjectEmployees(Long projectId);
    List<ProjectResponse>    getEmployeeProjects(Long employeeId);
}
