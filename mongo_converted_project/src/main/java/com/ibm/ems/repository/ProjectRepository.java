package com.ibm.ems.repository;

import com.ibm.ems.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjectRepository extends MongoRepository<Project, String> {

	List<Project> findByStatus(String status);

	List<Project> findByPriority(String priority);

	List<Project> findByLeadId(String leadId);
}