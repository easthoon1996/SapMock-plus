package com.dreamsecurity.sapmock.controller;

import com.dreamsecurity.sapmock.model.Employee;
import com.dreamsecurity.sapmock.service.FakeSapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/sap/opu/odata/sap/EMPLOYEE_BASIC_SRV")
public class EmployeeController {

    @Autowired
    private FakeSapService employeeService;

    private final RestTemplate restTemplate;

    public EmployeeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/sap/mock/generate-employees")
    public ResponseEntity<?> generateEmployees(@RequestParam int count) {
        employeeService.generateEmployees(count);
        // OData v2 스타일 JSON 응답

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> d = new HashMap<>();
        d.put("results", "success");
        response.put("d", d);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/Employees")
    public Map<String, Object> getEmployees(
            @RequestParam(name = "$skip", defaultValue = "0") int skip,
            @RequestParam(name = "$top", defaultValue = "10") int top,
            @RequestParam(name = "$filter", required = false) String filter) {

        String modifiedAfter = null;
        if (filter != null && filter.contains("hireDate gt")) {
            modifiedAfter = filter.split("'")[1];
        }

        List<Employee> results = employeeService.getEmployees(skip, top, modifiedAfter);

        // OData v2 스타일 JSON 응답
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> d = new HashMap<>();
        d.put("results", results);
        response.put("d", d);

        return response;
    }

    @PostMapping("/Employees")
    public Employee createEmployee(@RequestBody Employee newEmp) {
        return employeeService.addEmployee(newEmp);
    }

    @GetMapping("/Employees/{employeeId}")
    public ResponseEntity<?> getEmployeeById(@PathVariable String employeeId) {
        Optional<Employee> employee = employeeService.findEmployeeById(employeeId);
        if (employee.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> d = new HashMap<>();
            d.put("results", Collections.singletonList(employee.get()));
            response.put("d", d);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
