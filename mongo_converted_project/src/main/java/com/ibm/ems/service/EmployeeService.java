package com.ibm.ems.service;

import com.ibm.ems.dto.EmployeeRequest;
import com.ibm.ems.dto.EmployeeResponse;
import com.ibm.ems.exception.DuplicateResourceException;
import com.ibm.ems.exception.ResourceNotFoundException;
import com.ibm.ems.model.Department;
import com.ibm.ems.model.Employee;
import com.ibm.ems.model.EmployeeStatus;
import com.ibm.ems.repository.DepartmentRepository;
import com.ibm.ems.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

	private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

	private final EmployeeRepository employeeRepository;
	private final DepartmentRepository departmentRepository;

	public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
		this.employeeRepository = employeeRepository;
		this.departmentRepository = departmentRepository;
	}

	// ── CREATE ────────────────────────────────────────────────────────────────

	public EmployeeResponse create(EmployeeRequest request) {
		log.debug("Creating employee with email: {}", request.getEmail());

		if (employeeRepository.existsByEmail(request.getEmail())) {
			throw new DuplicateResourceException("Employee", "email", request.getEmail());
		}

		Employee employee = toEntity(request, new Employee());
		Employee saved = employeeRepository.save(employee);
		log.info("Created employee: id={}, email={}", saved.getId(), saved.getEmail());
		return EmployeeResponse.from(saved);
	}

	// ── READ ──────────────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public List<EmployeeResponse> findAll() {
		log.debug("Fetching all employees");
		return employeeRepository.findAll().stream().map(EmployeeResponse::from).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public EmployeeResponse findById(Long id) {
		log.debug("Finding employee by id: {}", id);
		Employee emp = employeeRepository.findByIdWithDepartment(id)
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
		return EmployeeResponse.from(emp);
	}

	@Transactional(readOnly = true)
	public EmployeeResponse findByEmail(String email) {
		Employee emp = employeeRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email));
		return EmployeeResponse.from(emp);
	}

	@Transactional(readOnly = true)
	public List<EmployeeResponse> findByDepartment(Long departmentId) {
		if (!departmentRepository.existsById(departmentId)) {
			throw new ResourceNotFoundException("Department", "id", departmentId);
		}
		return employeeRepository.findByDepartmentIdOrderByLastNameAsc(departmentId).stream()
				.map(EmployeeResponse::from).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<EmployeeResponse> findByStatus(EmployeeStatus status) {
		return employeeRepository.findByStatus(status).stream().map(EmployeeResponse::from)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Page<EmployeeResponse> search(String keyword, Pageable pageable) {
		log.debug("Searching employees with keyword: {}", keyword);
		return employeeRepository.searchByKeyword(keyword, pageable).map(EmployeeResponse::from);
	}

	@Transactional(readOnly = true)
	public List<EmployeeResponse> findBySalaryRange(BigDecimal min, BigDecimal max) {
		if (min.compareTo(max) > 0) {
			throw new IllegalArgumentException("minSalary must be less than or equal to maxSalary");
		}
		return employeeRepository.findBySalaryRange(min, max).stream().map(EmployeeResponse::from)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<EmployeeResponse> findHiredInYear(int year) {
		return employeeRepository.findHiredInYear(year).stream().map(EmployeeResponse::from)
				.collect(Collectors.toList());
	}

	// ── UPDATE ────────────────────────────────────────────────────────────────

	public EmployeeResponse update(Long id, EmployeeRequest request) {
		log.debug("Updating employee id: {}", id);

		Employee existing = employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

		// Check email uniqueness only if it changed
		if (!existing.getEmail().equalsIgnoreCase(request.getEmail())
				&& employeeRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
			throw new DuplicateResourceException("Employee", "email", request.getEmail());
		}

		toEntity(request, existing);
		Employee updated = employeeRepository.save(existing);
		log.info("Updated employee id={}", updated.getId());
		return EmployeeResponse.from(updated);
	}

	public EmployeeResponse updateSalary(Long id, BigDecimal newSalary) {
		Employee emp = employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
		log.info("Updating salary for id={}: {} -> {}", id, emp.getSalary(), newSalary);
		emp.setSalary(newSalary);
		return EmployeeResponse.from(employeeRepository.save(emp));
	}

	public EmployeeResponse updateStatus(Long id, EmployeeStatus status) {
		int updated = employeeRepository.updateStatus(id, status);
		if (updated == 0) {
			throw new ResourceNotFoundException("Employee", "id", id);
		}
		log.info("Status updated for employee id={} to {}", id, status);
		return findById(id);
	}

	public int applyRaise(Long departmentId, double percentage) {
		if (!departmentRepository.existsById(departmentId)) {
			throw new ResourceNotFoundException("Department", "id", departmentId);
		}
		if (percentage <= 0 || percentage > 100) {
			throw new IllegalArgumentException("Percentage must be between 0 and 100");
		}
		int count = employeeRepository.applyRaiseByDepartment(departmentId, percentage);
		log.info("Applied {}% raise to {} employees in dept id={}", percentage, count, departmentId);
		return count;
	}

	// ── DELETE ────────────────────────────────────────────────────────────────

	public void delete(Long id) {
		if (!employeeRepository.existsById(id)) {
			throw new ResourceNotFoundException("Employee", "id", id);
		}
		employeeRepository.deleteById(id);
		log.info("Deleted employee id={}", id);
	}

	// ── STATS ─────────────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public long count() {
		return employeeRepository.count();
	}

	@Transactional(readOnly = true)
	public BigDecimal getAvgSalaryByDepartment(Long departmentId) {
		if (!departmentRepository.existsById(departmentId)) {
			throw new ResourceNotFoundException("Department", "id", departmentId);
		}
		BigDecimal avg = employeeRepository.findAvgSalaryByDepartment(departmentId);
		return avg != null ? avg : BigDecimal.ZERO;
	}

	// ── Private helper: map request → entity ─────────────────────────────────

	private Employee toEntity(EmployeeRequest req, Employee emp) {
		emp.setFirstName(req.getFirstName());
		emp.setLastName(req.getLastName());
		emp.setEmail(req.getEmail());
		emp.setSalary(req.getSalary());
		emp.setJoinDate(req.getJoinDate());

		if (req.getStatus() != null) {
			emp.setStatus(req.getStatus());
		}

		if (req.getDepartmentId() != null) {
			Department dept = departmentRepository.findById(req.getDepartmentId())
					.orElseThrow(() -> new ResourceNotFoundException("Department", "id", req.getDepartmentId()));
			emp.setDepartment(dept);
		} else {
			emp.setDepartment(null);
		}

		return emp;
	}
}
