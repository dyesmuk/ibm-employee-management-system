//package com.ibm.ems.dto;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import com.ibm.ems.model.Employee;
//import com.ibm.ems.model.EmployeeStatus;
//import io.swagger.v3.oas.annotations.media.Schema;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@Schema(description = "Employee data returned by the API")
//public class EmployeeResponse {
//
//    @Schema(description = "Unique employee ID", example = "1")
//    private Long id;
//
//    @Schema(description = "First name", example = "Alice")
//    private String firstName;
//
//    @Schema(description = "Last name", example = "Smith")
//    private String lastName;
//
//    @Schema(description = "Full name", example = "Alice Smith")
//    private String fullName;
//
//    @Schema(description = "Email address", example = "alice.smith@ibm.com")
//    private String email;
//
//    @Schema(description = "Annual salary", example = "85000.00")
//    private BigDecimal salary;
//
//    @Schema(description = "Date of joining", example = "2022-01-15")
//    @JsonFormat(pattern = "yyyy-MM-dd")
//    private LocalDate joinDate;
//
//    @Schema(description = "Employment status", example = "ACTIVE")
//    private EmployeeStatus status;
//
//    @Schema(description = "Department ID", example = "1")
//    private Long departmentId;
//
//    @Schema(description = "Department name", example = "Engineering")
//    private String departmentName;
//
//    @Schema(description = "Department location", example = "Bangalore")
//    private String departmentLocation;
//
//    @Schema(description = "Record creation timestamp")
//    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
//    private LocalDateTime createdAt;
//
//    @Schema(description = "Record last updated timestamp")
//    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
//    private LocalDateTime updatedAt;
//
//    // ── Factory method ────────────────────────────────────────────────────────
//    public static EmployeeResponse from(Employee e) {
//        EmployeeResponse r = new EmployeeResponse();
//        r.id                 = e.getId();
//        r.firstName          = e.getFirstName();
//        r.lastName           = e.getLastName();
//        r.fullName           = e.getFullName();
//        r.email              = e.getEmail();
//        r.salary             = e.getSalary();
//        r.joinDate           = e.getJoinDate();
//        r.status             = e.getStatus();
//        r.createdAt          = e.getCreatedAt();
//        r.updatedAt          = e.getUpdatedAt();
//        if (e.getDepartment() != null) {
//            r.departmentId       = e.getDepartment().getId();
//            r.departmentName     = e.getDepartment().getName();
//            r.departmentLocation = e.getDepartment().getLocation();
//        }
//        return r;
//    }
//
//    // ── Getters ───────────────────────────────────────────────────────────────
//    public Long getId()                    { return id; }
//    public String getFirstName()           { return firstName; }
//    public String getLastName()            { return lastName; }
//    public String getFullName()            { return fullName; }
//    public String getEmail()               { return email; }
//    public BigDecimal getSalary()          { return salary; }
//    public LocalDate getJoinDate()         { return joinDate; }
//    public EmployeeStatus getStatus()      { return status; }
//    public Long getDepartmentId()          { return departmentId; }
//    public String getDepartmentName()      { return departmentName; }
//    public String getDepartmentLocation()  { return departmentLocation; }
//    public LocalDateTime getCreatedAt()    { return createdAt; }
//    public LocalDateTime getUpdatedAt()    { return updatedAt; }
//}
