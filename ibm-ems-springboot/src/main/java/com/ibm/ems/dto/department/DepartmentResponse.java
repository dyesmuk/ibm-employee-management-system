package com.ibm.ems.dto.department;

import com.ibm.ems.model.Department;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for Department.
 * DevSecOps: Returns only the fields the API consumer needs.
 * Internal JPA entity fields (lazy collections) are never exposed.
 */
@Schema(description = "Department data returned by the API")
public class DepartmentResponse {

    @Schema(description = "Department ID", example = "1")
    private Long id;

    @Schema(description = "Department name", example = "Engineering")
    private String name;

    @Schema(description = "Description", example = "Core software development team")
    private String description;

    /** Factory method — converts JPA entity to response DTO. */
    public static DepartmentResponse from(Department dept) {
        DepartmentResponse r = new DepartmentResponse();
        r.id          = dept.getId();
        r.name        = dept.getName();
        r.description = dept.getDescription();
        return r;
    }

    public Long   getId()          { return id; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
}
