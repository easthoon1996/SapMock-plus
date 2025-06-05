package com.dreamsecurity.sapmock.controller;

import com.dreamsecurity.sapmock.model.Employee;
import com.dreamsecurity.sapmock.service.FakeSapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/sap/opu/odata/sap/EMPLOYEE_BASIC_SRV")
public class EmployeeController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private FakeSapService employeeService;

    private final RestTemplate restTemplate;

    public EmployeeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/sap/mock/generate-employees")
    public ResponseEntity<?> generateEmployees(@RequestParam int count) {
        log.info("▶[generateEmployees] 요청: count={}", count);

        employeeService.generateEmployees(count);

        log.info("[generateEmployees] {}명의 Mock 사용자 생성 완료", count);

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
            @RequestParam(name = "$filter", required = false) String filter,
            HttpServletRequest request) {

        String clientIp = getClientIp(request);
        log.info("▶[getEmployees] 요청: skip={}, top={}, filter={}, from IP={}", skip, top, filter, clientIp);

        List<Employee> results = employeeService.getEmployees(skip, top, filter);
        log.info("[getEmployees] 결과: {}명의 직원 반환", results.size());

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> d = new HashMap<>();
        d.put("results", results);
        response.put("d", d);
        return response;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }


    @PostMapping("/Employees")
    public Employee createEmployee(@RequestBody Employee newEmp) {
        log.info("[createEmployee] 요청: {}", newEmp);

        Employee created = employeeService.addEmployee(newEmp);

        log.info("[createEmployee] 신규 직원 생성: {}", created.getEmployeeId());

        return created;
    }
}
