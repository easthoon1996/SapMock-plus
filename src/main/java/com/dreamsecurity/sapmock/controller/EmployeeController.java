package com.dreamsecurity.sapmock.controller;

import com.dreamsecurity.sapmock.model.Employee;
import com.dreamsecurity.sapmock.model.Privilege;
import com.dreamsecurity.sapmock.service.FakeSapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

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

    @Value("odata.metadata.domain")
    private String sapMetadataDomain;

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

        List<Employee> results = employeeService.findAllEmployees(skip, top, filter);
        log.info("[getEmployees] 결과: {}명의 직원 반환", results.size());

        List<Map<String, Object>> wrapped = results.stream().map(e -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("__metadata", Map.of(
                    "id", String.format(sapMetadataDomain + "/sap/opu/odata/sap/EMPLOYEE_BASIC_SRV/Employees('%s')", e.getEmployeeId()),
                    "type", "EMPLOYEE_BASIC_SRV.Employee"
            ));
            map.putAll(convertEmployeeToMap(e));
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> d = new HashMap<>();
        d.put("results", wrapped);
        response.put("d", d);
        return response;
    }

    private Map<String, Object> convertEmployeeToMap(Employee e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("employeeId", e.getEmployeeId());
        map.put("firstName", e.getFirstName());
        map.put("lastName", e.getLastName());
        map.put("middleName", e.getMiddleName());
        map.put("gender", e.getGender());
        map.put("nationality", e.getNationality());
        map.put("maritalStatus", e.getMaritalStatus());
        map.put("position", e.getPosition());
        map.put("jobTitle", e.getJobTitle());
        map.put("department", e.getDepartment());
        map.put("departmentName", e.getDepartmentName());
        map.put("birthDate", e.getBirthDate());
        map.put("hireDate", e.getHireDate());
        map.put("terminationDate", e.getTerminationDate());
        map.put("workEmail", e.getWorkEmail());
        map.put("workPhone", e.getWorkPhone());
        map.put("mobilePhone", e.getMobilePhone());
        map.put("address", e.getAddress());
        map.put("bankAccount", e.getBankAccount());
        map.put("taxId", e.getTaxId());
        return map;
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

        Employee created = employeeService.saveEmployee(newEmp);

        log.info("[createEmployee] 신규 직원 생성: {}", created.getEmployeeId());

        return created;
    }

    @GetMapping("/Employees/{employeeId}/Roles")
    public ResponseEntity<?> getEmployeeRoles(@PathVariable String employeeId) {
        return employeeService.findEmployeeById(employeeId)
                .map(emp -> {
                    List<Map<String, Object>> wrapped = emp.getRoles().stream().map(r -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("__metadata", Map.of("type", "EMPLOYEE_BASIC_SRV.Role"));
                        map.put("roleId", r.getRoleId());
                        map.put("roleName", r.getRoleName());
                        return map;
                    }).collect(Collectors.toList());
                    return ResponseEntity.ok(Map.of("d", Map.of("results", wrapped)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/Employees/{employeeId}/Privileges")
    public ResponseEntity<?> getEmployeePrivileges(@PathVariable String employeeId) {
        return employeeService.findEmployeeById(employeeId)
                .map(emp -> {
                    List<Privilege> privileges = emp.getRoles().stream()
                            .flatMap(r -> r.getPrivileges().stream())
                            .distinct()
                            .collect(Collectors.toList());
                    List<Map<String, Object>> wrapped = privileges.stream().map(p -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("__metadata", Map.of("type", "EMPLOYEE_BASIC_SRV.Privilege"));
                        map.put("privilegeId", p.getPrivilegeId());
                        map.put("privilegeName", p.getPrivilegeName());
                        return map;
                    }).collect(Collectors.toList());
                    return ResponseEntity.ok(Map.of("d", Map.of("results", wrapped)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/Employees/{employeeId}")
    public ResponseEntity<?> getEmployeeDetail(@PathVariable String employeeId) {
        log.info("[getEmployeeDetail] 요청: employeeId={}", employeeId);

        return employeeService.findEmployeeById(employeeId)
                .map(emp -> {
                    log.info("[getEmployeeDetail] 결과: {}", emp);
                    Map<String, Object> d = new LinkedHashMap<>();
                    d.put("__metadata", Map.of(
                            "id", String.format(sapMetadataDomain + "/sap/opu/odata/sap/EMPLOYEE_BASIC_SRV/Employees('%s')", emp.getEmployeeId()),
                            "type", "EMPLOYEE_BASIC_SRV.Employee"
                    ));
                    d.putAll(convertEmployeeToMap(emp));
                    return ResponseEntity.ok(Map.of("d", d));
                })
                .orElseGet(() -> {
                    log.warn("[getEmployeeDetail] 결과: 직원 없음");
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/Employees/{employeeId}/CheckAuthorization")
    public ResponseEntity<?> checkAuthorization(
            @PathVariable String employeeId,
            @RequestParam String object,
            @RequestParam String field,
            @RequestParam String value) {

        return employeeService.findEmployeeById(employeeId)
                .map(emp -> {
                    boolean hasAuth = emp.getRoles().stream()
                            .flatMap(role -> role.getPrivileges().stream())
                            .anyMatch(priv -> priv.getPrivilegeId().equals(object) &&
                                    Arrays.equals(priv.getPrivilegeName().split("="), new String[]{field, value}));

                    Map<String, Object> result = new HashMap<>();
                    result.put("employeeId", employeeId);
                    result.put("object", object);
                    result.put("field", field);
                    result.put("value", value);
                    result.put("hasAuthorization", hasAuth);
                    return ResponseEntity.ok(result);
                })
                .orElseGet(() -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("employeeId", employeeId);
                    result.put("object", object);
                    result.put("field", field);
                    result.put("value", value);
                    result.put("hasAuthorization", false);
                    result.put("note", "직원이 존재하지 않음");
                    return ResponseEntity.ok(result);
                });
    }
}
