package com.ibm.ems.dto.project;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ibm.ems.model.Project;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Project data returned by the API")
public class ProjectResponse {

    @Schema(example = "64a1b2c3d4e5f6a7b8c9d0e1") private String id;
    @Schema(example = "YONO 2.0")                  private String name;
    @Schema(example = "Next-gen mobile banking")   private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(example = "ACTIVE") private String status;

    public static ProjectResponse from(Project p) {
        ProjectResponse r = new ProjectResponse();
        r.id          = p.getId();
        r.name        = p.getName();
        r.description = p.getDescription();
        r.startDate   = p.getStartDate();
        r.endDate     = p.getEndDate();
        r.status      = p.getStatus() != null ? p.getStatus() : null;
        return r;
    }

    public String    getId()          { return id; }
    public String    getName()        { return name; }
    public String    getDescription() { return description; }
    public LocalDate getStartDate()   { return startDate; }
    public LocalDate getEndDate()     { return endDate; }
    public String    getStatus()      { return status; }
}
