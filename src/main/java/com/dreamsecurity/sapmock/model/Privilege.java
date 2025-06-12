package com.dreamsecurity.sapmock.model;

public class Privilege {

    private String privilegeId;      // 권한 객체 ID (예: S_USER_GRP)
    private String privilegeName;    // 필드=값 (예: ACTVT=01, TCD=SM30)
    private String description;      // 설명 (예: "사용자 그룹 생성")

    public Privilege() {}

    public Privilege(String privilegeId, String privilegeName, String description) {
        this.privilegeId = privilegeId;
        this.privilegeName = privilegeName;
        this.description = description;
    }

    public String getPrivilegeId() {
        return privilegeId;
    }

    public void setPrivilegeId(String privilegeId) {
        this.privilegeId = privilegeId;
    }

    public String getPrivilegeName() {
        return privilegeName;
    }

    public void setPrivilegeName(String privilegeName) {
        this.privilegeName = privilegeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
