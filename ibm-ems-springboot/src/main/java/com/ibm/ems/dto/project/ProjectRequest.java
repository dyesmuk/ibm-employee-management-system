package com.ibm.ems.dto.project;

import com.ibm.ems.model.Project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Request DTO for creating or updating a Project.
 */
@Schema(description = "Request body for creating or updating a project")
public class ProjectRequest {

    @Schema(description = "Project name", example = "YONO 2.0",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 150, message = "Name must be 2–150 characters")
    private String name;

    @Schema(description = "Project description", example = "Next-gen mobile banking platform")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Schema(description = "Project start date (YYYY-MM-DD)", example = "2024-01-15",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Schema(description = "Project end date (YYYY-MM-DD) — optional for ongoing projects",
            example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "Project status", example = "PLANNED",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Status is required")
    private ProjectStatus status;

    public String        getName()                       { return name; }
    public void          setName(String v)               { this.name = v; }
    public String        getDescription()                { return description; }
    public void          setDescription(String v)        { this.description = v; }
    public LocalDate     getStartDate()                  { return startDate; }
    public void          setStartDate(LocalDate v)       { this.startDate = v; }
    public LocalDate     getEndDate()                    { return endDate; }
    public void          setEndDate(LocalDate v)         { this.endDate = v; }
    public ProjectStatus getStatus()                     { return status; }
    public void          setStatus(ProjectStatus v)      { this.status = v; }
}
