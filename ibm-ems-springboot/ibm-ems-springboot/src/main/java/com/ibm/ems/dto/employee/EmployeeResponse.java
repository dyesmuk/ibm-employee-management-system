package com.ibm.ems.dto.employee;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ibm.ems.model.Department;
import com.ibm.ems.model.Employee;
import com.ibm.ems.model.Role;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Employee data returned by the API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeResponse {

    @Schema(description = "Unique employee ID", example = "64a1b2c3d4e5f6a7b8c9d0e1")
    private String id;

    @Schema(description = "First name", example = "Arjun")
    private String firstName;

    @Schema(description = "Last name", example = "Sharma")
    private String lastName;

    @Schema(description = "Full name", example = "Arjun Sharma")
    private String fullName;

    @Schema(description = "Work email address", example = "arjun.sharma@ibm.co.in")
    private String email;

    @Schema(description = "Phone number", example = "+919811001001")
    private String phone;

    @Schema(description = "Monthly gross salary (INR) — visible to ADMIN and self only",
            example = "55000.00", nullable = true)
    private BigDecimal salary;

    @Schema(description = "Date of joining", example = "2022-06-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @Schema(description = "Employment status", example = "ACTIVE")
    private String status;

    @Schema(description = "Department ID", example = "64a1b2c3d4e5f6a7b8c9d0e1")
    private String departmentId;

    @Schema(description = "Department name", example = "Engineering")
    private String departmentName;

    @Schema(description = "Role ID", example = "64a1b2c3d4e5f6a7b8c9d0e2")
    private String roleId;

    @Schema(description = "Role name", example = "SENIOR_ENGINEER")
    private String roleName;

    @Schema(description = "Record creation timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Record last updated timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Factory method — converts entity to DTO.
     * department and role are passed separately since Employee now stores only their IDs.
     */
    public static EmployeeResponse from(Employee emp, boolean includeSalary,
                                        Department department, Role role) {
        EmployeeResponse r = new EmployeeResponse();
        r.id        = emp.getId();
        r.firstName = emp.getFirstName();
        r.lastName  = emp.getLastName();
        r.fullName  = emp.getFirstName() + " " + emp.getLastName();
        r.email     = emp.getEmail();
        r.phone     = emp.getPhone();
        r.hireDate  = emp.getHireDate();
        r.status    = emp.getStatus() != null ? emp.getStatus().name() : null;
        r.createdAt = emp.getCreatedAt();
        r.updatedAt = emp.getUpdatedAt();
        r.salary    = includeSalary ? emp.getSalary() : null;
        if (department != null) {
            r.departmentId   = department.getId();
            r.departmentName = department.getName();
        }
        if (role != null) {
            r.roleId   = role.getId();
            r.roleName = role.getName();
        }
        return r;
    }

    /** Convenience overload when department/role are not needed. */
    public static EmployeeResponse from(Employee emp, boolean includeSalary) {
        return from(emp, includeSalary, null, null);
    }

    @Override
    public String toString() {
        return "EmployeeResponse{id=" + id + ", status=" + status + ", salary=[REDACTED]}";
    }

    public String        getId()             { return id; }
    public String        getFirstName()      { return firstName; }
    public String        getLastName()       { return lastName; }
    public String        getFullName()       { return fullName; }
    public String        getEmail()          { return email; }
    public String        getPhone()          { return phone; }
    public BigDecimal    getSalary()         { return salary; }
    public LocalDate     getHireDate()       { return hireDate; }
    public String        getStatus()         { return status; }
    public String        getDepartmentId()   { return departmentId; }
    public String        getDepartmentName() { return departmentName; }
    public String        getRoleId()         { return roleId; }
    public String        getRoleName()       { return roleName; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }
}
