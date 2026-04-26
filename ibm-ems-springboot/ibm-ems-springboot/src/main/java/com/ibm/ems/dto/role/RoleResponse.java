package com.ibm.ems.dto.role;

import com.ibm.ems.model.Role;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Role data returned by the API")
public class RoleResponse {

    @Schema(example = "64a1b2c3d4e5f6a7b8c9d0e1") private String  id;
    @Schema(example = "SENIOR_ENGINEER")            private String  name;
    @Schema(example = "Senior engineer")            private String  description;
    @Schema(example = "3")                          private Integer level;

    public static RoleResponse from(Role role) {
        RoleResponse r = new RoleResponse();
        r.id          = role.getId();
        r.name        = role.getName();
        r.description = role.getDescription();
        r.level       = role.getLevel();
        return r;
    }

    public String  getId()          { return id; }
    public String  getName()        { return name; }
    public String  getDescription() { return description; }
    public Integer getLevel()       { return level; }
}
