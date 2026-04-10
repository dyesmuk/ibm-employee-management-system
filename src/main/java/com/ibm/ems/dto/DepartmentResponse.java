package com.ibm.ems.dto;

import com.ibm.ems.model.Department;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Department data returned by the API")
public class DepartmentResponse {

	@Schema(example = "oid_01001")
	private String id; // ✅ FIXED

	@Schema(example = "Engineering")
	private String name;

	@Schema(example = "Bangalore")
	private String location;

	@Schema(description = "Number of employees in this department", example = "5")
	private int employeeCount;

	public static DepartmentResponse from(Department d, long count) {
		DepartmentResponse r = new DepartmentResponse();
		r.id = d.getId(); // ✅ now matches
		r.name = d.getName();
		r.location = d.getLocation();
		r.employeeCount = (int) count;
		return r;
	}

	public String getId() {
		return id;
	} // ✅ FIXED

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	public int getEmployeeCount() {
		return employeeCount;
	}
}