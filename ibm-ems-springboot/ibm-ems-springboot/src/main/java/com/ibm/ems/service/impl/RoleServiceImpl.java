package com.ibm.ems.service.impl;

import org.springframework.stereotype.Service;

import com.ibm.ems.dto.role.RoleRequest;
import com.ibm.ems.dto.role.RoleResponse;
import com.ibm.ems.exception.BadRequestException;
import com.ibm.ems.exception.ConflictException;
import com.ibm.ems.exception.ResourceNotFoundException;
import com.ibm.ems.model.Role;
import com.ibm.ems.repository.EmployeeRepository;
import com.ibm.ems.repository.RoleRepository;
import com.ibm.ems.service.RoleService;

import java.util.List;

@Service
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
    public RoleResponse getRoleById(String id) {
        return RoleResponse.from(findById(id));
    }

    @Override
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new ConflictException(
                "Role already exists with name = '" + request.getName() + "'");
        }
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setLevel(request.getLevel());
        return RoleResponse.from(roleRepository.save(role));
    }

    @Override
    public RoleResponse updateRole(String id, RoleRequest request) {
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
    public void deleteRole(String id) {
        findById(id);
        if (employeeRepository.existsByRoleId(id)) {
            throw new BadRequestException(
                "Cannot delete role: employees are currently assigned to it.");
        }
        roleRepository.deleteById(id);
    }

    private Role findById(String id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }
}
