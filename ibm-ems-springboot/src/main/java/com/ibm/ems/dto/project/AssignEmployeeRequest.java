package com.ibm.ems.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request body for assigning an employee to a project.
 *
 * DevSecOps: projectRole sent as request body, not @RequestParam.
 * Query parameters appear in server access logs — avoid for business data.
 */
@Schema(description = "Request body for assigning an employee to a project")
public class AssignEmployeeRequest {

    @Schema(description = "Employee ID to assign", example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @Schema(description = "Role of the employee on this project", example = "Backend Developer",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Project role is required")
    @Size(min = 2, max = 100, message = "Project role must be 2–100 characters")
    private String projectRole;

    @Schema(description = "Assignment date (YYYY-MM-DD)", example = "2024-03-01")
    @PastOrPresent(message = "Assignment date cannot be in the future")
    private LocalDate assignedDate;

    public Long      getEmployeeId()              { return employeeId; }
    public void      setEmployeeId(Long v)         { this.employeeId = v; }
    public String    getProjectRole()              { return projectRole; }
    public void      setProjectRole(String v)      { this.projectRole = v; }
    public LocalDate getAssignedDate()             { return assignedDate; }
    public void      setAssignedDate(LocalDate v)  { this.assignedDate = v; }
}
