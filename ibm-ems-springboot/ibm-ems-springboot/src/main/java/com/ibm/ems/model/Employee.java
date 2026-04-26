package com.ibm.ems.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "employees")
public class Employee {

    public enum EmployeeStatus { ACTIVE, INACTIVE, TERMINATED }

    @Id
    private String id;

    private String firstName;
    private String lastName;

    @Indexed(unique = true)
    private String email;

    private String phone;
    private BigDecimal salary;
    private LocalDate hireDate;
    private EmployeeStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String departmentId;
    private String roleId;
}
