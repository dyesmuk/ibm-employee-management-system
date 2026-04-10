package com.ibm.ems.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "roles")
public class Role {

    @Id
    private String id;

    private String title;
    private String level;
    private String domain;
    private long baseSalary;

    private List<String> permissions;

    private boolean isRemoteEligible;
    private int minExperienceYears;
    private int openings;

    private Instant createdAt;

    // Getters and Setters
}