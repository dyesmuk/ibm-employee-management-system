package com.ibm.ems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Department data")
public class DepartmentRequest {

    @Schema(description = "Department name", example = "Engineering", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
    private String name;

    @Schema(description = "Office location", example = "Bangalore")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }
    public String getLocation()              { return location; }
    public void setLocation(String location) { this.location = location; }
}
