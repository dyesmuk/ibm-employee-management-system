package com.ibm.ems.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.ems.model.Employee;
import com.ibm.ems.service.EmployeeService;

@Controller
@RequestMapping("/employees")
public class EmployeeWebController {

	@Autowired
	private EmployeeService employeeService;

	@GetMapping
	public String listEmployees(Model model) {
		try {
			List<Employee> employees = employeeService.findAll();
			model.addAttribute("employees", employees);
		} catch (Exception e) {
			model.addAttribute("employees", List.of());
			model.addAttribute("error", "Error fetching employees: " + e.getMessage());
		}
		return "employee-list";
	}

	@GetMapping("/{id}")
	public String viewEmployee(@PathVariable String id, Model model) {
		try {
			Employee employee = employeeService.findById(id);
			model.addAttribute("employee", employee);
			model.addAttribute("error", null);
		} catch (Exception e) {
			model.addAttribute("employee", null);
			model.addAttribute("error", "Employee with ID '" + id + "' not found");
		}
		return "employee-detail";
	}
}