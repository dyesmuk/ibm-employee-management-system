
package com.ibm.ems.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.ems.model.Employee;
import com.ibm.ems.service.EmployeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/employees")
//@CrossOrigin(origins = "*")
@Tag(name = "Employee API", description = "CRUD operations for Employee Management")
public class EmployeeController {

	@Autowired

	private EmployeeService employeeService;

	@Operation(summary = "Get all employees", description = "Returns a list of all employees")
	@ApiResponse(responseCode = "200", description = "List of employees retrieved successfully")
	@GetMapping
	public ResponseEntity<List<Employee>> getAll() {
		return ResponseEntity.ok(employeeService.findAll());
	}

	@Operation(summary = "Get employee by ID")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Employee found"),
			@ApiResponse(responseCode = "404", description = "Employee not found") })
	@GetMapping("/{id}")
	public ResponseEntity<Employee> getById(
			@Parameter(description = "Employee ID", example = "oid_03001") @PathVariable String id) {

		return ResponseEntity.ok(employeeService.findById(id));
	}

	@Operation(summary = "Get employee by email")
	@GetMapping("/by-email")
	public ResponseEntity<Employee> getByEmail(@RequestParam String email) {
		return ResponseEntity.ok(employeeService.findByEmail(email));
	}
}

//package com.ibm.ems.controller;
//
//import java.math.BigDecimal;
//import java.net.URI;
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PatchMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
//
//import com.ibm.ems.dto.EmployeeRequest;
//import com.ibm.ems.dto.EmployeeResponse;
//import com.ibm.ems.model.EmployeeStatus;
//import com.ibm.ems.service.EmployeeService;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.Max;
//import jakarta.validation.constraints.Min;
//
//@RestController
//@RequestMapping("/api/v1/employees")
//@CrossOrigin(origins = "*")
//@Tag(name = "Employee API", description = "CRUD operations for Employee Management")
//public class EmployeeController {
//
//	@Autowired
//
//	private EmployeeService employeeService;
//
//	@Operation(summary = "Get all employees", description = "Returns a list of all employees")
//	@ApiResponse(responseCode = "200", description = "List of employees retrieved successfully")
//	@GetMapping
//	public ResponseEntity<List<EmployeeResponse>> getAll() {
//		return ResponseEntity.ok(employeeService.findAll());
//	}
//
//	@Operation(summary = "Get employee by ID")
//	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Employee found"),
//			@ApiResponse(responseCode = "404", description = "Employee not found") })
//	@GetMapping("/{id}")
//	public ResponseEntity<EmployeeResponse> getById(
//			@Parameter(description = "Employee ID", example = "1") @PathVariable Long id) {
//		return ResponseEntity.ok(employeeService.findById(id));
//	}
//
//	@Operation(summary = "Get employee by email")
//	@GetMapping("/by-email")
//	public ResponseEntity<EmployeeResponse> getByEmail(@RequestParam String email) {
//		return ResponseEntity.ok(employeeService.findByEmail(email));
//	}
//
//
//	@Operation(summary = "Get employees by department ID")
//	@GetMapping("/department/{departmentId}")
//	public ResponseEntity<List<EmployeeResponse>> getByDepartment(@PathVariable String departmentId) {
//		return ResponseEntity.ok(employeeService.findByDepartment(departmentId));
//	}
//
//	// ── GET by status ─────────────────────────────────────────────────────────
//
//	@Operation(summary = "Get employees by status", description = "Valid statuses: ACTIVE, ON_LEAVE, TERMINATED")
//	@GetMapping("/status/{status}")
//	public ResponseEntity<List<EmployeeResponse>> getByStatus(@PathVariable EmployeeStatus status) {
//		return ResponseEntity.ok(employeeService.findByStatus(status));
//	}
//
//	// ── GET — search with pagination ──────────────────────────────────────────
//
//	@Operation(summary = "Search employees", description = "Full-text search across first name, last name, and email. Supports pagination and sorting.")
//	@GetMapping("/search")
//	public ResponseEntity<Page<EmployeeResponse>> search(@RequestParam(defaultValue = "") String keyword,
//			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
//			@RequestParam(defaultValue = "lastName") String sortBy,
//			@RequestParam(defaultValue = "asc") String direction) {
//
//		Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//
//		Pageable pageable = PageRequest.of(page, size, sort);
//		return ResponseEntity.ok(employeeService.search(keyword, pageable));
//	}
//
//	// ── GET — salary range ────────────────────────────────────────────────────
//
//	@Operation(summary = "Get employees within a salary range")
//	@GetMapping("/salary-range")
//	public ResponseEntity<List<EmployeeResponse>> getBySalaryRange(@RequestParam BigDecimal min,
//			@RequestParam BigDecimal max) {
//		return ResponseEntity.ok(employeeService.findBySalaryRange(min, max));
//	}
//
//	// ── GET — hired in year ───────────────────────────────────────────────────
//
//	@Operation(summary = "Get employees hired in a specific year")
//	@GetMapping("/hired-in/{year}")
//	public ResponseEntity<List<EmployeeResponse>> getHiredInYear(@PathVariable @Min(2000) @Max(2100) int year) {
//		return ResponseEntity.ok(employeeService.findHiredInYear(year));
//	}
//
//	// ── GET — count ───────────────────────────────────────────────────────────
//
//	@Operation(summary = "Get total employee count")
//	@GetMapping("/count")
//	public ResponseEntity<Map<String, Long>> getCount() {
//		return ResponseEntity.ok(Map.of("totalEmployees", employeeService.count()));
//	}
//
//	// ── GET — average salary by department ───────────────────────────────────
//
//	@Operation(summary = "Get average salary for a department")
//	@GetMapping("/department/{departmentId}/avg-salary")
//	public ResponseEntity<Map<String, Object>> getAvgSalary(@PathVariable Long departmentId) {
//		BigDecimal avg = employeeService.getAvgSalaryByDepartment(departmentId);
//		return ResponseEntity.ok(Map.of("departmentId", departmentId, "averageSalary", avg));
//	}
//
//	// ── POST — create ─────────────────────────────────────────────────────────
//
//	@Operation(summary = "Create a new employee")
//	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Employee created successfully"),
//			@ApiResponse(responseCode = "400", description = "Validation error"),
//			@ApiResponse(responseCode = "409", description = "Email already exists") })
//	@PostMapping
//	public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
//		EmployeeResponse created = employeeService.create(request);
//		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId())
//				.toUri();
//		return ResponseEntity.created(location).body(created);
//	}
//
//	// ── PUT — full update ─────────────────────────────────────────────────────
//
//	@Operation(summary = "Update an employee (full update)")
//	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Employee updated"),
//			@ApiResponse(responseCode = "400", description = "Validation error"),
//			@ApiResponse(responseCode = "404", description = "Employee not found"),
//			@ApiResponse(responseCode = "409", description = "Email already in use") })
//	@PutMapping("/{id}")
//	public ResponseEntity<EmployeeResponse> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
//		return ResponseEntity.ok(employeeService.update(id, request));
//	}
//
//	// ── PATCH — update salary only ────────────────────────────────────────────
//
//	@Operation(summary = "Update salary only (partial update)")
//	@PatchMapping("/{id}/salary")
//	public ResponseEntity<EmployeeResponse> updateSalary(@PathVariable Long id,
//			@RequestBody Map<String, BigDecimal> body) {
//		BigDecimal newSalary = body.get("salary");
//		if (newSalary == null || newSalary.compareTo(BigDecimal.ZERO) <= 0) {
//			throw new IllegalArgumentException("salary must be a positive number");
//		}
//		return ResponseEntity.ok(employeeService.updateSalary(id, newSalary));
//	}
//
//	// ── PATCH — update status only ────────────────────────────────────────────
//
//	@Operation(summary = "Update employee status (partial update)", description = "Valid statuses: ACTIVE, ON_LEAVE, TERMINATED")
//	@PatchMapping("/{id}/status")
//	public ResponseEntity<EmployeeResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
//		EmployeeStatus status = EmployeeStatus.valueOf(body.getOrDefault("status", "ACTIVE").toUpperCase());
//		return ResponseEntity.ok(employeeService.updateStatus(id, status));
//	}
//
//	// ── POST — apply raise to entire department ───────────────────────────────
//
//	@Operation(summary = "Apply a salary raise (%) to all employees in a department")
//	@PostMapping("/department/{departmentId}/raise")
//	public ResponseEntity<Map<String, Object>> applyRaise(@PathVariable Long departmentId,
//			@RequestParam double percentage) {
//		int count = employeeService.applyRaise(departmentId, percentage);
//		return ResponseEntity.ok(Map.of("departmentId", departmentId, "raisePercentage", percentage, "employeesUpdated",
//				count, "message", "Salary raise applied successfully"));
//	}
//
//	// ── DELETE ────────────────────────────────────────────────────────────────
//
//	@Operation(summary = "Delete an employee by ID")
//	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Employee deleted"),
//			@ApiResponse(responseCode = "404", description = "Employee not found") })
//	@DeleteMapping("/{id}")
//	public ResponseEntity<Void> delete(@PathVariable Long id) {
//		employeeService.delete(id);
//		return ResponseEntity.noContent().build();
//	}
//}
