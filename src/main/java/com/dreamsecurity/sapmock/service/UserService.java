package com.dreamsecurity.sapmock.service;

import com.dreamsecurity.sapmock.model.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final List<User> users = new ArrayList<>();

    public UserService() {
        users.add(new User("U001", "홍길동", "hong@company.com", "HR", "2025-06-01T12:00:00Z"));
        users.add(new User("U002", "이영희", "lee@company.com", "IT", "2025-06-02T08:00:00Z"));
    }

    public List<User> getUsers(int skip, int top, String modifiedAtFilter) {
        List<User> filtered = users;
        if (modifiedAtFilter != null) {
            Instant modifiedAt = Instant.parse(modifiedAtFilter);
            filtered = users.stream()
                    .filter(u -> Instant.parse(u.getModifiedAt()).isAfter(modifiedAt))
                    .collect(Collectors.toList());
        }
        int end = Math.min(skip + top, filtered.size());
        if (skip >= filtered.size()) {
            return new ArrayList<>();
        }
        return filtered.subList(skip, end);
    }

    public User getUser(String userId) {
        return users.stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }
}
