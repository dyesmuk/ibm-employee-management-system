package com.ibm.ems.dto.project;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ibm.ems.model.Project;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Project data returned by the API")
public class ProjectResponse {

    @Schema(example = "1")         private Long   id;
    @Schema(example = "YONO 2.0") private String name;
    @Schema(example = "Next-gen mobile banking platform") private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(example = "ACTIVE")   private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static ProjectResponse from(Project p) {
        ProjectResponse r = new ProjectResponse();
        r.id          = p.getId();
        r.name        = p.getName();
        r.description = p.getDescription();
        r.startDate   = p.getStartDate();
        r.endDate     = p.getEndDate();
        r.status      = p.getStatus().name();
        r.createdAt   = p.getCreatedAt();
        r.updatedAt   = p.getUpdatedAt();
        return r;
    }

    public Long          getId()          { return id; }
    public String        getName()        { return name; }
    public String        getDescription() { return description; }
    public LocalDate     getStartDate()   { return startDate; }
    public LocalDate     getEndDate()     { return endDate; }
    public String        getStatus()      { return status; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }
}
