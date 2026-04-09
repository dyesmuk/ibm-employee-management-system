package com.ibm.ems.repository;

import com.ibm.ems.model.Employee;
import com.ibm.ems.model.Employee.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // ── Simple finders (derived from method name) ─────────────────────────────

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Employee> findByStatus(EmployeeStatus status);

    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByDepartmentName(String departmentName);

    long countByDepartmentId(Long departmentId);

    List<Employee> findByDepartmentIdOrderByLastNameAsc(Long departmentId);

    // ── Search by name (case-insensitive) ─────────────────────────────────────

    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // ── Salary filters ────────────────────────────────────────────────────────

    List<Employee> findBySalaryGreaterThanEqual(BigDecimal minSalary);

    List<Employee> findBySalaryLessThanEqual(BigDecimal maxSalary);

    // ── JPQL queries ──────────────────────────────────────────────────────────

    @Query("SELECT e FROM Employee e WHERE e.salary BETWEEN :min AND :max ORDER BY e.salary DESC")
    List<Employee> findBySalaryRange(@Param("min") BigDecimal min,
                                     @Param("max") BigDecimal max);

    @Query("SELECT e FROM Employee e JOIN FETCH e.department d WHERE e.id = :id")
    Optional<Employee> findByIdWithDepartment(@Param("id") Long id);

    @Query("SELECT e FROM Employee e JOIN FETCH e.department d " +
           "WHERE d.id = :deptId AND e.status = :status")
    List<Employee> findByDepartmentAndStatus(@Param("deptId") Long deptId,
                                              @Param("status") EmployeeStatus status);

    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.lastName)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.email)     LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Employee> searchByKeyword(@Param("q") String query, Pageable pageable);

    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department.id = :deptId")
    BigDecimal findAvgSalaryByDepartment(@Param("deptId") Long deptId);

    // ── Bulk update (modifying) ───────────────────────────────────────────────

    @Modifying
    @Query("UPDATE Employee e SET e.salary = e.salary * (1 + :pct / 100.0) " +
           "WHERE e.department.id = :deptId")
    int applyRaiseByDepartment(@Param("deptId") Long deptId,
                                @Param("pct") double percentage);

    @Modifying
    @Query("UPDATE Employee e SET e.status = :status WHERE e.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") EmployeeStatus status);

    // ── Native SQL ────────────────────────────────────────────────────────────

    @Query(value = "SELECT * FROM employees WHERE YEAR(join_date) = :year",
           nativeQuery = true)
    List<Employee> findHiredInYear(@Param("year") int year);
}
