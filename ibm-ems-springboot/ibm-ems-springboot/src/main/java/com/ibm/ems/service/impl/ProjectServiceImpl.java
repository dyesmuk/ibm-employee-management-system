package com.ibm.ems.service.impl;

import org.springframework.stereotype.Service;

import com.ibm.ems.dto.employee.EmployeeResponse;
import com.ibm.ems.dto.project.AssignEmployeeRequest;
import com.ibm.ems.dto.project.ProjectRequest;
import com.ibm.ems.dto.project.ProjectResponse;
import com.ibm.ems.exception.BadRequestException;
import com.ibm.ems.exception.ConflictException;
import com.ibm.ems.exception.InvalidStateTransitionException;
import com.ibm.ems.exception.ResourceNotFoundException;
import com.ibm.ems.model.Department;
import com.ibm.ems.model.Employee;
import com.ibm.ems.model.EmployeeProject;
import com.ibm.ems.model.Project;
import com.ibm.ems.model.ProjectStatus;
import com.ibm.ems.model.Role;
import com.ibm.ems.repository.DepartmentRepository;
import com.ibm.ems.repository.EmployeeProjectRepository;
import com.ibm.ems.repository.EmployeeRepository;
import com.ibm.ems.repository.ProjectRepository;
import com.ibm.ems.repository.RoleRepository;
import com.ibm.ems.service.ProjectService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Map<ProjectStatus, Set<ProjectStatus>> ALLOWED_TRANSITIONS =
        Map.of(
            ProjectStatus.PLANNED,   Set.of(ProjectStatus.ACTIVE,   ProjectStatus.CANCELLED),
            ProjectStatus.ACTIVE,    Set.of(ProjectStatus.ON_HOLD,  ProjectStatus.COMPLETED, ProjectStatus.CANCELLED),
            ProjectStatus.ON_HOLD,   Set.of(ProjectStatus.ACTIVE,   ProjectStatus.CANCELLED),
            ProjectStatus.COMPLETED, Set.of(),
            ProjectStatus.CANCELLED, Set.of()
        );

    private final ProjectRepository         projectRepository;
    private final EmployeeRepository        employeeRepository;
    private final EmployeeProjectRepository employeeProjectRepository;
    private final DepartmentRepository      departmentRepository;
    private final RoleRepository            roleRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                               EmployeeRepository employeeRepository,
                               EmployeeProjectRepository employeeProjectRepository,
                               DepartmentRepository departmentRepository,
                               RoleRepository roleRepository) {
        this.projectRepository         = projectRepository;
        this.employeeRepository        = employeeRepository;
        this.employeeProjectRepository = employeeProjectRepository;
        this.departmentRepository      = departmentRepository;
        this.roleRepository            = roleRepository;
    }

    @Override
    public List<ProjectResponse> getAllProjects(ProjectStatus status) {
        List<Project> projects = (status == null)
                ? projectRepository.findAll()
                : projectRepository.findByStatus(status.name());
        return projects.stream().map(ProjectResponse::from).toList();
    }

    @Override
    public ProjectResponse getProjectById(String id) {
        return ProjectResponse.from(findById(id));
    }

    @Override
    public ProjectResponse createProject(ProjectRequest req) {
        if (projectRepository.existsByName(req.getName())) {
            throw new ConflictException(
                "Project already exists with name = '" + req.getName() + "'");
        }
        validateDates(req.getStartDate(), req.getEndDate());

        Project project = new Project();
        project.setName(req.getName());
        project.setDescription(req.getDescription());
        project.setStartDate(req.getStartDate());
        project.setEndDate(req.getEndDate());
        project.setStatus(req.getStatus().name());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Override
    public ProjectResponse updateProject(String id, ProjectRequest req) {
        Project existing = findById(id);
        ProjectStatus currentStatus = ProjectStatus.valueOf(existing.getStatus());
        if (!currentStatus.equals(req.getStatus())) {
            validateStateTransition(currentStatus, req.getStatus());
        }
        if (!existing.getName().equals(req.getName())
                && projectRepository.existsByName(req.getName())) {
            throw new ConflictException(
                "Project already exists with name = '" + req.getName() + "'");
        }
        validateDates(req.getStartDate(), req.getEndDate());

        existing.setName(req.getName());
        existing.setDescription(req.getDescription());
        existing.setStartDate(req.getStartDate());
        existing.setEndDate(req.getEndDate());
        existing.setStatus(req.getStatus().name());
        return ProjectResponse.from(projectRepository.save(existing));
    }

    @Override
    public void deleteProject(String id) {
        findById(id);
        employeeProjectRepository.findByProjectId(id)
                .forEach(ep -> employeeProjectRepository.deleteById(ep.getId()));
        projectRepository.deleteById(id);
    }

    @Override
    public void assignEmployee(String projectId, AssignEmployeeRequest req) {
        Project project = findById(projectId);
        Employee employee = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Employee", "id", req.getEmployeeId()));

        ProjectStatus status = ProjectStatus.valueOf(project.getStatus());
        if (status == ProjectStatus.CANCELLED || status == ProjectStatus.COMPLETED) {
            throw new BadRequestException(
                "Cannot assign employees to a project with status: " + project.getStatus());
        }

        if (employeeProjectRepository.existsByEmployeeIdAndProjectId(
                employee.getId(), projectId)) {
            throw new ConflictException(
                "Employee " + employee.getId() + " is already assigned to project " + projectId);
        }

        LocalDate assignedDate = (req.getAssignedDate() != null)
                ? req.getAssignedDate() : LocalDate.now();

        EmployeeProject ep = new EmployeeProject();
        ep.setEmployeeId(employee.getId());
        ep.setProjectId(projectId);
        ep.setAssignedDate(assignedDate);
        ep.setProjectRole(req.getProjectRole());
        employeeProjectRepository.save(ep);
    }

    @Override
    public void removeEmployee(String projectId, String employeeId) {
        findById(projectId);
        EmployeeProject ep = employeeProjectRepository
                .findByEmployeeIdAndProjectId(employeeId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Assignment not found for employee " + employeeId + " on project " + projectId));
        employeeProjectRepository.deleteById(ep.getId());
    }

    @Override
    public List<EmployeeResponse> getProjectEmployees(String projectId) {
        findById(projectId);
        return employeeProjectRepository.findByProjectId(projectId).stream()
                .map(ep -> {
                    Employee emp = employeeRepository.findById(ep.getEmployeeId()).orElse(null);
                    if (emp == null) return null;
                    Department dept = emp.getDepartmentId() != null
                            ? departmentRepository.findById(emp.getDepartmentId()).orElse(null) : null;
                    Role role = emp.getRoleId() != null
                            ? roleRepository.findById(emp.getRoleId()).orElse(null) : null;
                    return EmployeeResponse.from(emp, false, dept, role);
                })
                .filter(r -> r != null)
                .toList();
    }

    @Override
    public List<ProjectResponse> getEmployeeProjects(String employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        return employeeProjectRepository.findByEmployeeId(employeeId).stream()
                .map(ep -> projectRepository.findById(ep.getProjectId()).map(ProjectResponse::from).orElse(null))
                .filter(r -> r != null)
                .toList();
    }

    private Project findById(String id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    private void validateStateTransition(ProjectStatus from, ProjectStatus to) {
        Set<ProjectStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidStateTransitionException(
                String.format("Invalid project status transition: %s → %s. Allowed from %s: %s",
                    from, to, from, allowed.isEmpty() ? "none (terminal state)" : allowed));
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && !endDate.isAfter(startDate)) {
            throw new BadRequestException("End date must be after start date.");
        }
    }
}
