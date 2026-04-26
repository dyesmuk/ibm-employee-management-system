package com.ibm.ems.dto.department;

import com.ibm.ems.model.Department;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Department data returned by the API")
public class DepartmentResponse {

    @Schema(description = "Department ID", example = "64a1b2c3d4e5f6a7b8c9d0e1")
    private String id;

    @Schema(description = "Department name", example = "Engineering")
    private String name;

    @Schema(description = "Description", example = "Core software development team")
    private String description;

    public static DepartmentResponse from(Department dept) {
        DepartmentResponse r = new DepartmentResponse();
        r.id          = dept.getId();
        r.name        = dept.getName();
        r.description = dept.getDescription();
        return r;
    }

    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
}
