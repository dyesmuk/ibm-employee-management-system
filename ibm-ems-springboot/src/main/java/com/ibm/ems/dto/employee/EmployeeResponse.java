package com.ibm.ems.dto.employee;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ibm.ems.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for Employee.
 *
 * DevSecOps (A01 — Broken Access Control, A02 — Cryptographic Failures):
 *
 *   PII MASKING — The 'salary' field is sensitive PII.
 *   Business rule: visible only to HR Admins and the employee themselves.
 *
 *   BEFORE (vulnerable):
 *     // Raw Employee entity returned — salary always included
 *     return ResponseEntity.ok(employeeService.getEmployeeById(id));
 *
 *   AFTER (secure):
 *     - EmployeeResponse.from(employee, includeSalary)
 *     - When includeSalary=false → salary field is null and omitted from JSON
 *     - @JsonInclude(NON_NULL) ensures null salary is not serialized as null
 *     - Controller determines includeSalary based on Authentication roles
 *
 *   LOGGING — toString() never includes salary or email (PII fields).
 *   The AOP LoggingAspect logs method names and timing, not entity fields.
 */
@Schema(description = "Employee data returned by the API")
@JsonInclude(JsonInclude.Include.NON_NULL)   // null fields (masked salary) omitted from JSON
public class EmployeeResponse {

    @Schema(description = "Unique employee ID", example = "1")
    private Long id;

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

    /**
     * PII field — null when the caller is not authorised to see salary.
     * @JsonInclude(NON_NULL) ensures it is omitted from the JSON response entirely.
     */
    @Schema(description = "Monthly gross salary (INR) — visible to ADMIN and self only",
            example = "55000.00", nullable = true)
    private BigDecimal salary;

    @Schema(description = "Date of joining", example = "2022-06-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @Schema(description = "Employment status", example = "ACTIVE")
    private String status;

    @Schema(description = "Department ID", example = "1")
    private Long departmentId;

    @Schema(description = "Department name", example = "Engineering")
    private String departmentName;

    @Schema(description = "Role ID", example = "3")
    private Long roleId;

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
     *
     * @param emp           the Employee entity
     * @param includeSalary true for ADMIN or self; false for all other callers
     */
    public static EmployeeResponse from(Employee emp, boolean includeSalary) {
        EmployeeResponse r = new EmployeeResponse();
        r.id             = emp.getId();
        r.firstName      = emp.getFirstName();
        r.lastName       = emp.getLastName();
        r.fullName       = emp.getFirstName() + " " + emp.getLastName();
        r.email          = emp.getEmail();
        r.phone          = emp.getPhone();
        r.hireDate       = emp.getHireDate();
        r.status         = emp.getStatus().name();
        r.createdAt      = emp.getCreatedAt();
        r.updatedAt      = emp.getUpdatedAt();

        // PII masking — salary only included for authorised callers
        r.salary = includeSalary ? emp.getSalary() : null;

        if (emp.getDepartment() != null) {
            r.departmentId   = emp.getDepartment().getId();
            r.departmentName = emp.getDepartment().getName();
        }
        if (emp.getRole() != null) {
            r.roleId   = emp.getRole().getId();
            r.roleName = emp.getRole().getName();
        }
        return r;
    }

    /**
     * DevSecOps (A02): toString() NEVER includes salary, email, or phone.
     * Prevents accidental PII leakage into logs via AOP or debug output.
     */
    @Override
    public String toString() {
        return "EmployeeResponse{id=" + id + ", status=" + status + ", salary=[REDACTED]}";
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public Long          getId()             { return id; }
    public String        getFirstName()      { return firstName; }
    public String        getLastName()       { return lastName; }
    public String        getFullName()       { return fullName; }
    public String        getEmail()          { return email; }
    public String        getPhone()          { return phone; }
    public BigDecimal    getSalary()         { return salary; }
    public LocalDate     getHireDate()       { return hireDate; }
    public String        getStatus()         { return status; }
    public Long          getDepartmentId()   { return departmentId; }
    public String        getDepartmentName() { return departmentName; }
    public Long          getRoleId()         { return roleId; }
    public String        getRoleName()       { return roleName; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }
}
