package com.ibm.ems.dto.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ibm.ems.model.Employee.EmployeeStatus;

@Schema(description = "Request body for creating or updating an employee")
public class EmployeeRequest {

    @Schema(description = "First name", example = "Arjun", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2–50 characters")
    private String firstName;

    @Schema(description = "Last name", example = "Sharma", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be 2–50 characters")
    private String lastName;

    @Schema(description = "Work email", example = "arjun.sharma@ibm.co.in",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "Phone number (E.164 format)", example = "+919811001001")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$",
             message = "Phone must be 10–15 digits, optionally starting with +")
    private String phone;

    @Schema(description = "Monthly gross salary (INR)", example = "55000.00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Salary format invalid")
    private BigDecimal salary;

    @Schema(description = "Date of joining (YYYY-MM-DD)", example = "2022-06-01",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    @Schema(description = "Employment status", example = "ACTIVE",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Status is required")
    private EmployeeStatus status;

    @Schema(description = "Department ID", example = "64a1b2c3d4e5f6a7b8c9d0e1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Department ID is required")
    private String departmentId;

    @Schema(description = "Role ID", example = "64a1b2c3d4e5f6a7b8c9d0e2",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Role ID is required")
    private String roleId;

    public String         getFirstName()                       { return firstName; }
    public void           setFirstName(String v)               { this.firstName = v; }
    public String         getLastName()                        { return lastName; }
    public void           setLastName(String v)                { this.lastName = v; }
    public String         getEmail()                           { return email; }
    public void           setEmail(String v)                   { this.email = v; }
    public String         getPhone()                           { return phone; }
    public void           setPhone(String v)                   { this.phone = v; }
    public BigDecimal     getSalary()                          { return salary; }
    public void           setSalary(BigDecimal v)              { this.salary = v; }
    public LocalDate      getHireDate()                        { return hireDate; }
    public void           setHireDate(LocalDate v)             { this.hireDate = v; }
    public EmployeeStatus getStatus()                          { return status; }
    public void           setStatus(EmployeeStatus v)          { this.status = v; }
    public String         getDepartmentId()                    { return departmentId; }
    public void           setDepartmentId(String v)            { this.departmentId = v; }
    public String         getRoleId()                          { return roleId; }
    public void           setRoleId(String v)                  { this.roleId = v; }
}
