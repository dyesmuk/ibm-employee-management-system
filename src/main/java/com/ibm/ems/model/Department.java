
package com.ibm.ems.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Document(collection = "departments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Department {

    @Id
    private String id;

    private String name;
    private String division;
    private String location;
    private String state;
    private int headCount;
    private long annualBudget;
    private String description;
    private Instant established;
    private boolean isActive;
    private String managerId;

    // getters/setters
}
