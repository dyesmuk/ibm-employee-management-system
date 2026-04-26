package com.ibm.ems.repository;

import com.ibm.ems.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends MongoRepository<Employee, String> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartmentId(String departmentId);

    List<Employee> findByRoleId(String roleId);

    List<Employee> findByStatus(Employee.EmployeeStatus status);

    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    boolean existsByDepartmentId(String departmentId);

    boolean existsByRoleId(String roleId);
}
