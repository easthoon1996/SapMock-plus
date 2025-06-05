package com.dreamsecurity.sapmock.model;

import java.util.ArrayList;
import java.util.List;

public class Role {
    private String roleId;
    private String roleName;
    private String description;
    private List<Privilege> privileges = new ArrayList<>();

    // Getter/Setter
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Privilege> getPrivileges() { return privileges; }
    public void setPrivileges(List<Privilege> privileges) { this.privileges = privileges; }
}
