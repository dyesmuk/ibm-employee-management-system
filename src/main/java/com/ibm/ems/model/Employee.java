
package com.ibm.ems.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Document(collection = "employees")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private int age;

    private String departmentId;
    private String roleId;
    private List<String> projectIds;

    private long salary;
    private Instant joiningDate;

    private String status;
    private List<String> skills;

    private double performanceScore;

    private Address address;

    private boolean isRemote;
    private List<String> certifications;

    private boolean managerApproval;

    // getters/setters
}
