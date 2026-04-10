package com.ibm.ems.repository;

import com.ibm.ems.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RoleRepository extends MongoRepository<Role, String> {

    List<Role> findByDomain(String domain);

    List<Role> findByLevel(String level);
}