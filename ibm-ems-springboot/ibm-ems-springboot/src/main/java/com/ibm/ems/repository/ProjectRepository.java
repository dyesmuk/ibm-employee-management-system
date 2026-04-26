package com.ibm.ems.repository;

import com.ibm.ems.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.List;

public interface ProjectRepository extends MongoRepository<Project, String> {

    Optional<Project> findByName(String name);

    List<Project> findByStatus(String status);

    boolean existsByName(String name);
}
