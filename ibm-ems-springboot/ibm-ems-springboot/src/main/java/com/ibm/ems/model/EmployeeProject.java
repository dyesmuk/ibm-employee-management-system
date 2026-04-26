package com.ibm.ems.model;

import java.time.LocalDate;

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
@Document(collection = "employee_projects")
public class EmployeeProject {

    @Id
    private String id;

    @Indexed
    private String employeeId;

    @Indexed
    private String projectId;

    private LocalDate assignedDate;

    private String projectRole;
}
