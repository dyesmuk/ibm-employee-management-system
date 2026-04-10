package com.ibm.ems.repository;

import com.ibm.ems.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {

	Optional<Employee> findByEmail(String email);

}

//package com.ibm.ems.repository;
//
//import com.ibm.ems.dto.EmployeeResponse;
//import com.ibm.ems.model.Employee;
//import com.ibm.ems.model.EmployeeStatus;
//
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.mongodb.repository.MongoRepository;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Stream;
//
//public interface EmployeeRepository extends MongoRepository<Employee, String> {
//
//	Optional<Employee> findByEmail(String email);
//
//	boolean existsByEmail(String email);
//
//	long countByDepartmentId(String departmentId);
//
//	List<Employee> findByDepartmentId(String departmentId);
//
//	List<Employee> findByStatus(EmployeeStatus status);
//
//	List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
//
//	List<Employee> findBySalaryBetween(long min, long max);
//
//	List<Employee> findBySalaryGreaterThanEqual(long min);
//
//	List<Employee> findBySalaryLessThanEqual(long max);
//
//	Optional<Employee> findByIdWithDepartment(Long id);
//
//	Collection<EmployeeResponse> findByDepartmentIdOrderByLastNameAsc(Long departmentId);
//
//	Stream<Employee> searchByKeyword(String keyword, Pageable pageable);
//}