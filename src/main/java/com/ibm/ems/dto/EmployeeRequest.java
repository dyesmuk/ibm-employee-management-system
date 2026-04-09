package com.ibm.ems.dto;

import com.ibm.ems.model.Employee.EmployeeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// ─────────────────────────────────────────────────────────────────────────────
// REQUEST DTO — what the client sends
// ─────────────────────────────────────────────────────────────────────────────
@Schema(description = "Request body for creating or updating an employee")
public class EmployeeRequest {

    @Schema(description = "First name", example = "Alice", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2–50 characters")
    private String firstName;

    @Schema(description = "Last name", example = "Smith", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be 2–50 characters")
    private String lastName;

    @Schema(description = "Work email address", example = "alice.smith@ibm.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @Schema(description = "Annual salary in INR", example = "75000.00", minimum = "1000")
    @NotNull(message = "Salary is required")
    @DecimalMin(value = "1000.00", message = "Salary must be at least 1,000")
    @DecimalMax(value = "9999999.99", message = "Salary is too large")
    private BigDecimal salary;

    @Schema(description = "Date of joining (YYYY-MM-DD)", example = "2024-01-15")
    @NotNull(message = "Join date is required")
    @PastOrPresent(message = "Join date cannot be in the future")
    private LocalDate joinDate;

    @Schema(description = "ID of the department", example = "1")
    private Long departmentId;

    @Schema(description = "Employee status", example = "ACTIVE")
    private EmployeeStatus status;

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getFirstName()                     { return firstName; }
    public void setFirstName(String firstName)       { this.firstName = firstName; }

    public String getLastName()                      { return lastName; }
    public void setLastName(String lastName)         { this.lastName = lastName; }

    public String getEmail()                         { return email; }
    public void setEmail(String email)               { this.email = email; }

    public BigDecimal getSalary()                    { return salary; }
    public void setSalary(BigDecimal salary)         { this.salary = salary; }

    public LocalDate getJoinDate()                   { return joinDate; }
    public void setJoinDate(LocalDate joinDate)      { this.joinDate = joinDate; }

    public Long getDepartmentId()                    { return departmentId; }
    public void setDepartmentId(Long departmentId)   { this.departmentId = departmentId; }

    public EmployeeStatus getStatus()                { return status; }
    public void setStatus(EmployeeStatus status)     { this.status = status; }
}
