package com.ibm.ems.controller;

import com.ibm.ems.dto.DepartmentRequest;
import com.ibm.ems.dto.DepartmentResponse;
import com.ibm.ems.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/departments")
@CrossOrigin(origins = "*")
@Tag(name = "Department API", description = "CRUD operations for Departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // ── GET all ───────────────────────────────────────────────────────────────

    @Operation(summary = "Get all departments")
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAll() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    // ── GET by ID ─────────────────────────────────────────────────────────────

    @Operation(summary = "Get department by ID (includes employee count)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department found"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.findById(id));
    }

    // ── GET headcount report ──────────────────────────────────────────────────

    @Operation(summary = "Get employee headcount per department")
    @GetMapping("/headcount")
    public ResponseEntity<Map<String, Long>> getHeadcount() {
        return ResponseEntity.ok(departmentService.getHeadcountReport());
    }

    // ── POST — create ─────────────────────────────────────────────────────────

    @Operation(summary = "Create a new department")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Department created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Department name already exists")
    })
    @PostMapping
    public ResponseEntity<DepartmentResponse> create(
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse created = departmentService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // ── PUT — full update ─────────────────────────────────────────────────────

    @Operation(summary = "Update a department")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department updated"),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "409", description = "Department name already in use")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Operation(summary = "Delete a department",
               description = "Fails if the department has employees. Reassign employees first.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Department deleted"),
        @ApiResponse(responseCode = "400", description = "Department still has employees"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
