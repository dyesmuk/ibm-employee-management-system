package com.ibm.ems.service;

import java.util.List;

import com.ibm.ems.dto.role.RoleRequest;
import com.ibm.ems.dto.role.RoleResponse;

public interface RoleService {
    List<RoleResponse> getAllRoles();
    RoleResponse       getRoleById(String id);
    RoleResponse       createRole(RoleRequest request);
    RoleResponse       updateRole(String id, RoleRequest request);
    void               deleteRole(String id);
}
