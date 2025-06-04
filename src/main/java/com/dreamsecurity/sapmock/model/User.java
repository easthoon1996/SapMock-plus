package com.dreamsecurity.sapmock.model;

public class User {
    private String userId;
    private String name;
    private String email;
    private String department;
    private String modifiedAt;

    public User() {}

    public User(String userId, String name, String email, String department, String modifiedAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.modifiedAt = modifiedAt;
    }

    // Getters & Setters 생략
}
