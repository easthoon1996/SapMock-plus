package com.dreamsecurity.sapmock.model;

public class Privilege {
    private String privilegeId;
    private String privilegeName;
    private String description;

    // Getter/Setter
    public String getPrivilegeId() { return privilegeId; }
    public void setPrivilegeId(String privilegeId) { this.privilegeId = privilegeId; }

    public String getPrivilegeName() { return privilegeName; }
    public void setPrivilegeName(String privilegeName) { this.privilegeName = privilegeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
