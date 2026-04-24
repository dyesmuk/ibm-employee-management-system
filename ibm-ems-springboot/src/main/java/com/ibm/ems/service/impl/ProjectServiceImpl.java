package com.ibm.ems.service.impl;

import com.ibm.ems.dto.employee.EmployeeResponse;
import com.ibm.ems.dto.project.AssignEmployeeRequest;
import com.ibm.ems.dto.project.ProjectRequest;
import com.ibm.ems.dto.project.ProjectResponse;
import com.ibm.ems.exception.BadRequestException;
import com.ibm.ems.exception.ConflictException;
import com.ibm.ems.exception.InvalidStateTransitionException;
import com.ibm.ems.exception.ResourceNotFoundException;
import com.ibm.ems.model.Employee;
import com.ibm.ems.model.EmployeeProject;
import com.ibm.ems.model.Project;
import com.ibm.ems.model.Project.ProjectStatus;
import com.ibm.ems.repository.EmployeeProjectRepository;
import com.ibm.ems.repository.EmployeeRepository;
import com.ibm.ems.repository.ProjectRepository;
import com.ibm.ems.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Project business logic.
 *
 * DevSecOps fixes:
 *
 *  A04 — Insecure Design: Project State Machine
 *    BEFORE: updateProject() set any status directly with no validation.
 *            An attacker or client error could move PLANNED → COMPLETED,
 *            bypassing ACTIVE state — a real banking workflow violation.
 *
 *    AFTER:  validateStateTransition() enforces the allowed transitions:
 *              PLANNED  → ACTIVE, CANCELLED
 *              ACTIVE   → ON_HOLD, COMPLETED, CANCELLED
 *              ON_HOLD  → ACTIVE, CANCELLED
 *              COMPLETED → (terminal — no transitions)
 *              CANCELLED → (terminal — no transitions)
 *
 *  A01 — Business rules that prevent data abuse:
 *    - Employee cannot be assigned to the same project twice
 *    - End date must be after start date
 *    - Project name must be unique
 *    - Project deletion cascades to remove all employee assignments
 */
@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    // Valid state transitions — key: from, value: allowed 'to' states
    private static final java.util.Map<ProjectStatus, Set<ProjectStatus>> ALLOWED_TRANSITIONS =
        java.util.Map.of(
            ProjectStatus.PLANNED,   Set.of(ProjectStatus.ACTIVE,   ProjectStatus.CANCELLED),
            ProjectStatus.ACTIVE,    Set.of(ProjectStatus.ON_HOLD,  ProjectStatus.COMPLETED, ProjectStatus.CANCELLED),
            ProjectStatus.ON_HOLD,   Set.of(ProjectStatus.ACTIVE,   ProjectStatus.CANCELLED),
            ProjectStatus.COMPLETED, Set.of(),   // terminal
            ProjectStatus.CANCELLED, Set.of()    // terminal
        );

    private final ProjectRepository         projectRepository;
    private final EmployeeRepository        employeeRepository;
    private final EmployeeProjectRepository employeeProjectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                               EmployeeRepository employeeRepository,
                               EmployeeProjectRepository employeeProjectRepository) {
        this.projectRepository         = projectRepository;
        this.employeeRepository        = employeeRepository;
        this.employeeProjectRepository = employeeProjectRepository;
    }

    @Override
    public List<ProjectResponse> getAllProjects(ProjectStatus status) {
        List<Project> projects = (status == null)
                ? projectRepository.findAll()
                : projectRepository.findByStatus(status);
        return projects.stream().map(ProjectResponse::from).toList();
    }

    @Override
    public ProjectResponse getProjectById(Long id) {
        return ProjectResponse.from(findById(id));
    }

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest req) {
        if (projectRepository.existsByName(req.getName())) {
            throw new ConflictException(
                "Project already exists with name = '" + req.getName() + "'");
        }
        validateDates(req.getStartDate(), req.getEndDate());

        Project project = Project.builder()
                .name(req.getName())
                .description(req.getDescription())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(req.getStatus())
                .build();
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest req) {
        Project existing = findById(id);

        // Enforce state machine — A04: Insecure Design fix
        if (!existing.getStatus().equals(req.getStatus())) {
            validateStateTransition(existing.getStatus(), req.getStatus());
        }

        // Unique name check if name changed
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
        existing.setStatus(req.getStatus());
        return ProjectResponse.from(projectRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        findById(id);
        // Cascade: all employee_project rows for this project are deleted by orphanRemoval
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void assignEmployee(Long projectId, AssignEmployeeRequest req) {
        Project project = findById(projectId);
        Employee employee = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Employee", "id", req.getEmployeeId()));

        // Business rule: cannot assign to a CANCELLED or COMPLETED project
        if (project.getStatus() == ProjectStatus.CANCELLED
                || project.getStatus() == ProjectStatus.COMPLETED) {
            throw new BadRequestException(
                "Cannot assign employees to a project with status: " + project.getStatus());
        }

        // Business rule: no duplicate assignments
        EmployeeProject.EmployeeProjectId epId =
                new EmployeeProject.EmployeeProjectId(employee.getId(), projectId);
        if (employeeProjectRepository.existsById(epId)) {
            throw new ConflictException(
                "Employee " + employee.getId() + " is already assigned to project " + projectId);
        }

        LocalDate assignedDate = (req.getAssignedDate() != null)
                ? req.getAssignedDate() : LocalDate.now();

        EmployeeProject ep = EmployeeProject.builder()
                .id(epId)
                .employee(employee)
                .project(project)
                .assignedDate(assignedDate)
                .projectRole(req.getProjectRole())
                .build();

        employeeProjectRepository.save(ep);
    }

    @Override
    @Transactional
    public void removeEmployee(Long projectId, Long employeeId) {
        findById(projectId); // confirm project exists
        EmployeeProject.EmployeeProjectId epId =
                new EmployeeProject.EmployeeProjectId(employeeId, projectId);
        if (!employeeProjectRepository.existsById(epId)) {
            throw new ResourceNotFoundException(
                "Assignment not found for employee " + employeeId
                + " on project " + projectId);
        }
        employeeProjectRepository.deleteById(epId);
    }

    @Override
    public List<EmployeeResponse> getProjectEmployees(Long projectId) {
        findById(projectId);
        return employeeProjectRepository.findByProjectId(projectId).stream()
                .map(ep -> EmployeeResponse.from(ep.getEmployee(), false))
                .toList();
    }

    @Override
    public List<ProjectResponse> getEmployeeProjects(Long employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        return employeeProjectRepository.findByEmployeeId(employeeId).stream()
                .map(ep -> ProjectResponse.from(ep.getProject()))
                .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    /**
     * Enforce the project lifecycle state machine.
     *
     * DevSecOps (A04 — Insecure Design):
     *   This is a DESIGN-LEVEL security control — an illegal transition
     *   is rejected regardless of who calls the API.
     *
     *   Key rule from the courseware: PLANNED → COMPLETED is explicitly
     *   blocked. The project must pass through ACTIVE first.
     */
    private void validateStateTransition(ProjectStatus from, ProjectStatus to) {
        Set<ProjectStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidStateTransitionException(
                String.format(
                    "Invalid project status transition: %s → %s. " +
                    "Allowed transitions from %s: %s",
                    from, to, from, allowed.isEmpty() ? "none (terminal state)" : allowed));
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && !endDate.isAfter(startDate)) {
            throw new BadRequestException(
                "End date must be after start date.");
        }
    }
}
