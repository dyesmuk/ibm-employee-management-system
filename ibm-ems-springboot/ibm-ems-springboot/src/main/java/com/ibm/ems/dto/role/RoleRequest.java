package com.ibm.ems.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request body for creating or updating a role")
public class RoleRequest {

    @Schema(example = "SENIOR_ENGINEER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
    private String name;

    @Schema(example = "Senior software engineer responsible for architecture decisions")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Schema(description = "Seniority level 1–10", example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Level is required")
    @Min(value = 1,  message = "Level must be at least 1")
    @Max(value = 10, message = "Level must not exceed 10")
    private Integer level;

    public String  getName()               { return name; }
    public void    setName(String v)       { this.name = v; }
    public String  getDescription()        { return description; }
    public void    setDescription(String v){ this.description = v; }
    public Integer getLevel()              { return level; }
    public void    setLevel(Integer v)     { this.level = v; }
}
