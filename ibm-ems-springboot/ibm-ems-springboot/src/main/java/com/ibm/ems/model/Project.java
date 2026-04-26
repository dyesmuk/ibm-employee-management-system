package com.ibm.ems.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

@Document(collection = "projects")
public class Project {

    @Id
    private String id;

    private String name;
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status;
}
