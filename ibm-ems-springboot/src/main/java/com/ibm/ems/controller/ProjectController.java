package com.ibm.ems.controller;

import com.ibm.ems.dto.employee.EmployeeResponse;
import com.ibm.ems.dto.project.AssignEmployeeRequest;
import com.ibm.ems.dto.project.ProjectRequest;
import com.ibm.ems.dto.project.ProjectResponse;
import com.ibm.ems.model.Project.ProjectStatus;
import com.ibm.ems.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Project REST controller.
 *
 * DevSecOps fix (A04 — Insecure Design):
 *   The state machine validation is enforced in ProjectServiceImpl.
 *   This controller simply delegates — business rules live in the service layer,
 *   not in the controller.  Controllers must NOT contain business logic.
 */
@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Project management and employee assignment endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "Get all projects",
               description = "Optionally filter by status: PLANNED, ACTIVE, ON_HOLD, COMPLETED, CANCELLED")
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @RequestParam(required = false) ProjectStatus status) {
        return ResponseEntity.ok(projectService.getAllProjects(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new project — ADMIN only")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update project details or status — ADMIN only",
               description = "State machine enforced: PLANNED→ACTIVE→COMPLETED. " +
                             "Direct PLANNED→COMPLETED is rejected (422).")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a project — ADMIN only",
               description = "Cascades: removes all employee assignments for this project.")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/employees")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign an employee to a project — ADMIN only")
    public ResponseEntity<Void> assignEmployee(
            @PathVariable Long projectId,
            @Valid @RequestBody AssignEmployeeRequest request) {
        projectService.assignEmployee(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{projectId}/employees/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove an employee from a project — ADMIN only")
    public ResponseEntity<Void> removeEmployee(
            @PathVariable Long projectId,
            @PathVariable Long employeeId) {
        projectService.removeEmployee(projectId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/employees")
    @Operation(summary = "Get all employees assigned to a project")
    public ResponseEntity<List<EmployeeResponse>> getProjectEmployees(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectEmployees(projectId));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get all projects assigned to an employee")
    public ResponseEntity<List<ProjectResponse>> getEmployeeProjects(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(projectService.getEmployeeProjects(employeeId));
    }
}
