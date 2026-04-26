package com.ibm.ems.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating or updating a department")
public class DepartmentRequest {

    @Schema(description = "Department name", example = "Engineering",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
    private String name;

    @Schema(description = "Department description", example = "Core software development team")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    public String getName()                  { return name; }
    public void   setName(String name)       { this.name = name; }
    public String getDescription()           { return description; }
    public void   setDescription(String d)   { this.description = d; }
}
