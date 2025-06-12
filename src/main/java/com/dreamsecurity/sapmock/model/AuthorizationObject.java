package com.dreamsecurity.sapmock.model;

import java.util.List;

public class AuthorizationObject {

    private String objectId;      // 권한 객체 ID (예: S_USER_GRP)
    private String description;   // 권한 객체 설명
    private List<String> fields;  // 권한 객체가 포함하는 필드 (예: ACTVT, TCD 등)

    // 생성자
    public AuthorizationObject(String objectId, String description, List<String> fields) {
        this.objectId = objectId;
        this.description = description;
        this.fields = fields;
    }

    // 기본 생성자 (직렬화/역직렬화를 위해)
    public AuthorizationObject() {}

    // Getter/Setter
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}
