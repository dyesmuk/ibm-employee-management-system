package com.ibm.ems.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ibm.ems.dto.employee.EmployeeRequest;
import com.ibm.ems.dto.employee.EmployeeResponse;
import com.ibm.ems.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employees", description = "Employee management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "Get all employees (paginated)",
               description = "Salary shown only for ADMIN role. Use ?page=0&size=10&sort=lastName,asc")
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(
            Pageable pageable, Authentication auth) {
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable, isAdmin(auth)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID",
               description = "Salary visible to ADMIN or to the employee themselves.")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id, isAdmin(auth)));
    }

    @GetMapping("/department/{deptId}")
    @Operation(summary = "Get all employees in a department")
    public ResponseEntity<List<EmployeeResponse>> getByDepartment(
            @PathVariable String deptId, Authentication auth) {
        return ResponseEntity.ok(employeeService.getEmployeesByDepartment(deptId, isAdmin(auth)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search employees by name",
               description = "Case-insensitive partial match on first or last name.")
    public ResponseEntity<List<EmployeeResponse>> search(
            @RequestParam
            @NotBlank(message = "Search term is required")
            @Size(max = 100, message = "Search term must not exceed 100 characters")
            String name,
            Authentication auth) {
        return ResponseEntity.ok(employeeService.searchEmployees(name, isAdmin(auth)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create (onboard) a new employee — ADMIN only")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(employeeService.createEmployee(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update employee details — ADMIN only")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable String id,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Terminate employee (soft delete, status=TERMINATED) — ADMIN only",
               description = "Soft delete: sets employee status to TERMINATED. Record is retained for audit history.")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
