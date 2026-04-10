package com.ibm.ems.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Document(collection = "projects")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    private String id;

    private String name;
    private String description;
    private String status;
    private String priority;

    private Instant startDate;
    private Instant endDate;

    private long budget;
    private long spentBudget;

    private List<String> techStack;

    private String leadId;
    private List<String> teamMemberIds;

    private int teamSize;
    private int milestones;
    private int completedMilestones;

    private List<String> tags;
    private boolean clientFacing;

    private Instant createdAt;

    // Getters and Setters
}