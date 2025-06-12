package com.dreamsecurity.sapmock.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

@Entity
public class Role {

    @Id
    private String roleId;         // 역할 ID (예: ADMIN)
    private String roleName;       // 역할 이름
    private String description;    // 설명

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "role_privilege",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id"))
    private List<Privilege> privileges = new ArrayList<>();  // 권한 리스트

    public Role() {}

    public Role(String roleId, String roleName, String description, List<Privilege> privileges) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
        this.privileges = privileges;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }
}
