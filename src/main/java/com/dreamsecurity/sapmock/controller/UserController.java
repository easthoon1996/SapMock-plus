package com.dreamsecurity.sapmock.controller;

import com.dreamsecurity.sapmock.model.User;
import com.dreamsecurity.sapmock.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sap")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public Map<String, Object> getUsers(
            @RequestParam(name = "$skip", defaultValue = "0") int skip,
            @RequestParam(name = "$top", defaultValue = "10") int top,
            @RequestParam(name = "$filter", required = false) String filter) {

        String modifiedAtFilter = null;
        if (filter != null && filter.contains("modifiedAt gt")) {
            modifiedAtFilter = filter.split("'")[1];
        }

        List<User> results = userService.getUsers(skip, top, modifiedAtFilter);

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> d = new HashMap<>();
        d.put("results", results);
        response.put("d", d);
        return response;
    }

    @GetMapping("/users/{userId}")
    public Map<String, Object> getUser(@PathVariable String userId) {
        User user = userService.getUser(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("d", user);
        return response;
    }
}
