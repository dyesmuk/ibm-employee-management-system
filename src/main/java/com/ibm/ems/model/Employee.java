package com.ibm.ems.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees",
       indexes = {
           @Index(name = "idx_email",  columnList = "email"),
           @Index(name = "idx_dept_id", columnList = "department_id")
       })
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2–50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be 2–50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "1000.00", message = "Salary must be at least 1,000")
    @DecimalMax(value = "9999999.99", message = "Salary is too large")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salary;

    @NotNull(message = "Join date is required")
    @PastOrPresent(message = "Join date cannot be in the future")
    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Lifecycle hooks ───────────────────────────────────────────────────────
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (joinDate == null) joinDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Enum ──────────────────────────────────────────────────────────────────
    public enum EmployeeStatus {
        ACTIVE, ON_LEAVE, TERMINATED
    }

    // ── Transient helper ──────────────────────────────────────────────────────
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ── Constructors ──────────────────────────────────────────────────────────
    public Employee() {}

    public Employee(String firstName, String lastName, String email,
                    BigDecimal salary, LocalDate joinDate, Department department) {
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.email      = email;
        this.salary     = salary;
        this.joinDate   = joinDate;
        this.department = department;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }

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

    public EmployeeStatus getStatus()                { return status; }
    public void setStatus(EmployeeStatus status)     { this.status = status; }

    public Department getDepartment()                { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public LocalDateTime getCreatedAt()              { return createdAt; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", name='" + getFullName() +
               "', email='" + email + "', status=" + status + "}";
    }
}
