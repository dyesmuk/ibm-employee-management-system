//package com.ibm.ems.repository;
//
//import com.ibm.ems.model.Department;
//import org.springframework.data.mongodb.repository.MongoRepository;
//
//import java.util.Optional;
//import java.util.List;
//
//public interface DepartmentRepository extends MongoRepository<Department, String> {
//
//	Optional<Department> findByName(String name);
//
//	boolean existsByName(String name);
//
//	List<Department> findByLocationIgnoreCase(String location);
//
//	List<Department> findByNameContainingIgnoreCase(String keyword);
//
//	boolean existsById(Long departmentId);
//}