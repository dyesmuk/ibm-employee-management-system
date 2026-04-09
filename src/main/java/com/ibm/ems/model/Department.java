package com.ibm.ems.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Department name is required")
	@Size(min = 2, max = 100, message = "Name must be 2–100 characters")
	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Size(max = 100)
	@Column(length = 100)
	private String location;

	@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
	@JsonIgnore
	private List<Employee> employees = new ArrayList<>();

	// ── Constructors ──────────────────────────────────────────────────────────
	public Department() {
	}

	public Department(String name, String location) {
		this.name = name;
		this.location = location;
	}

	// ── Getters & Setters ─────────────────────────────────────────────────────
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(List<Employee> e) {
		this.employees = e;
	}

	@Override
	public String toString() {
		return "Department{id=" + id + ", name='" + name + "', location='" + location + "'}";
	}
}
