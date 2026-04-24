package com.ibm.ems.service.impl;

import com.ibm.ems.dto.role.RoleRequest;
import com.ibm.ems.dto.role.RoleResponse;
import com.ibm.ems.exception.BadRequestException;
import com.ibm.ems.exception.ConflictException;
import com.ibm.ems.exception.ResourceNotFoundException;
import com.ibm.ems.model.Role;
import com.ibm.ems.repository.EmployeeRepository;
import com.ibm.ems.repository.RoleRepository;
import com.ibm.ems.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository     roleRepository;
    private final EmployeeRepository employeeRepository;

    public RoleServiceImpl(RoleRepository roleRepository,
                            EmployeeRepository employeeRepository) {
        this.roleRepository     = roleRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        return RoleResponse.from(findById(id));
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new ConflictException(
                "Role already exists with name = '" + request.getName() + "'");
        }
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .level(request.getLevel())
                .build();
        return RoleResponse.from(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role existing = findById(id);
        if (!existing.getName().equals(request.getName())
                && roleRepository.existsByName(request.getName())) {
            throw new ConflictException(
                "Role already exists with name = '" + request.getName() + "'");
        }
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setLevel(request.getLevel());
        return RoleResponse.from(roleRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        findById(id);
        if (employeeRepository.existsByRoleId(id)) {
            throw new BadRequestException(
                "Cannot delete role: employees are currently assigned to it.");
        }
        roleRepository.deleteById(id);
    }

    private Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }
}
