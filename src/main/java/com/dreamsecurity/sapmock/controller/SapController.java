package com.dreamsecurity.sapmock.controller;

import com.dreamsecurity.sapmock.model.Employee;
import com.dreamsecurity.sapmock.model.Privilege;
import com.dreamsecurity.sapmock.model.Role;
import com.dreamsecurity.sapmock.service.EmployeeGenerationService;
import com.dreamsecurity.sapmock.service.EmployeeQueryService;
import com.dreamsecurity.sapmock.service.EmployeeFilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sap/opu/odata/sap/EMPLOYEE_BASIC_SRV")
public class SapController {

    private static final Logger log = LoggerFactory.getLogger(SapController.class);

    private final EmployeeGenerationService generationService;
    private final EmployeeQueryService queryService;
    private final EmployeeFilterUtil filterUtil;
    private final RestTemplate restTemplate;

    @Autowired
    public SapController(EmployeeGenerationService generationService,
                              EmployeeQueryService queryService,
                              EmployeeFilterUtil filterUtil,
                              RestTemplate restTemplate) {
        this.generationService = generationService;
        this.queryService = queryService;
        this.filterUtil = filterUtil;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/sap/mock/generate-employees")
    public ResponseEntity<?> generateEmployees(@RequestParam int count) {
        log.info("▶[generateEmployees] 요청: count={}", count);
        generationService.generateEmployees(count);
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

        List<Employee> all = queryService.findAll();

        if (filter != null && !filter.isBlank()) {
            all = applyFilter(all, filter);
        }

        List<Employee> results = all.stream().skip(skip).limit(top).collect(Collectors.toList());
        log.info("[getEmployees] 결과: {}명의 직원 반환", results.size());

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> d = new HashMap<>();
        d.put("results", results);
        response.put("d", d);
        return response;
    }

/*    @GetMapping("/Employees/{employeeId}")
    public ResponseEntity<?> getEmployeeDetail(@PathVariable String employeeId) {
        log.info("[getEmployeeDetail] 요청: employeeId={}", employeeId);
        return queryService.findById(employeeId)
                .map(emp -> {
                    log.info("[getEmployeeDetail] 결과: {}", emp);
                    return ResponseEntity.ok(emp);
                })
                .orElseGet(() -> {
                    log.warn("[getEmployeeDetail] 결과: 직원 없음");
                    return ResponseEntity.notFound().build();
                });
    }*/

    @GetMapping("/Employees/{employeeId}")
    public ResponseEntity<?> getEmployeeDetail(@PathVariable String employeeId) {
        log.info("[getEmployeeDetail] 요청: employeeId={}", employeeId);
        return queryService.findById(employeeId)
                .map(emp -> {
                    log.info("[getEmployeeDetail] 결과: {}", emp);
                    Map<String, Object> response = new HashMap<>();
                    response.put("d", emp);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("[getEmployeeDetail] 결과: 직원 없음");
                    Map<String, Object> response = new HashMap<>();
                    Map<String, String> errorDetail = new HashMap<>();
                    errorDetail.put("code", "NotFound");
                    errorDetail.put("message", "Employee not found");
                    response.put("error", errorDetail);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }


/*    @GetMapping("/Employees/{employeeId}/Roles")
    public ResponseEntity<?> getEmployeeRoles(@PathVariable String employeeId) {
        return queryService.findById(employeeId)
                .map(Employee::getRoles)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/Employees/{employeeId}/Privileges")
    public ResponseEntity<?> getEmployeePrivileges(@PathVariable String employeeId) {
        return queryService.findById(employeeId)
                .map(emp -> {
                    List<Privilege> privileges = emp.getRoles().stream()
                            .flatMap(r -> r.getPrivileges().stream())
                            .distinct()
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(privileges);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }*/

    @GetMapping("/Employees/{employeeId}/Roles")
    public ResponseEntity<?> getEmployeeRoles(@PathVariable String employeeId) {
        return queryService.findById(employeeId)
                .map(emp -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("d", emp.getRoles());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> error = new HashMap<>();
                    Map<String, String> detail = new HashMap<>();
                    detail.put("code", "NotFound");
                    detail.put("message", "Employee roles not found");
                    error.put("error", detail);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                });
    }

    @GetMapping("/Employees/{employeeId}/Privileges")
    public ResponseEntity<?> getEmployeePrivileges(@PathVariable String employeeId) {
        return queryService.findById(employeeId)
                .map(emp -> {
                    List<Privilege> privileges = emp.getRoles().stream()
                            .flatMap(r -> r.getPrivileges().stream())
                            .distinct()
                            .collect(Collectors.toList());

                    Map<String, Object> response = new HashMap<>();
                    response.put("d", privileges);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> error = new HashMap<>();
                    Map<String, String> detail = new HashMap<>();
                    detail.put("code", "NotFound");
                    detail.put("message", "Employee privileges not found");
                    error.put("error", detail);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                });
    }


    @GetMapping("/Employees/{employeeId}/CheckAuthorization")
    public ResponseEntity<?> checkAuthorization(
            @PathVariable String employeeId,
            @RequestParam String object,
            @RequestParam String field,
            @RequestParam String value) {

        return queryService.findById(employeeId)
                .map(emp -> {
                    boolean hasAuth = emp.getRoles().stream()
                            .flatMap(role -> role.getPrivileges().stream())
                            .anyMatch(priv -> priv.getPrivilegeId().equals(object) &&
                                    priv.getPrivilegeName().equals(field + "=" + value));

                    Map<String, Object> result = new HashMap<>();
                    result.put("employeeId", employeeId);
                    result.put("object", object);
                    result.put("field", field);
                    result.put("value", value);
                    result.put("hasAuthorization", hasAuth);

                    // OData2 스타일
                    Map<String, Object> odata = new HashMap<>();
                    odata.put("d", result);
                    return ResponseEntity.ok(odata);
                })
                .orElseGet(() -> {
                    // OData2 에러 구조
                    Map<String, Object> error = new HashMap<>();
                    Map<String, String> detail = new HashMap<>();
                    detail.put("code", "NotFound");
                    detail.put("message", "직원이 존재하지 않음");
                    error.put("error", detail);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                });
    }


    private List<Employee> applyFilter(List<Employee> employees, String filter) {
        String[] conditions = filter.split(" and ");
        for (String condition : conditions) {
            condition = condition.trim();

            // 연산자별로 분기 처리
            if (condition.contains(" eq ")) {
                String[] parts = condition.split(" eq ");
                String field = parts[0].trim();
                String value = parts[1].replace("'", "").trim();
                employees = employees.stream()
                        .filter(e -> filterUtil.matchEquals(e, field, value))
                        .collect(Collectors.toList());
            } else if (condition.contains(" le ")) {
                String[] parts = condition.split(" le ");
                String field = parts[0].trim();
                String value = parts[1].replace("'", "").trim();
                employees = employees.stream()
                        .filter(e -> filterUtil.matchComparison(e, field, "le", value))
                        .collect(Collectors.toList());
            } else if (condition.contains(" lt ")) {
                String[] parts = condition.split(" lt ");
                String field = parts[0].trim();
                String value = parts[1].replace("'", "").trim();
                employees = employees.stream()
                        .filter(e -> filterUtil.matchComparison(e, field, "lt", value))
                        .collect(Collectors.toList());
            } else if (condition.contains(" ge ")) {
                String[] parts = condition.split(" ge ");
                String field = parts[0].trim();
                String value = parts[1].replace("'", "").trim();
                employees = employees.stream()
                        .filter(e -> filterUtil.matchComparison(e, field, "ge", value))
                        .collect(Collectors.toList());
            } else if (condition.contains(" gt ")) {
                String[] parts = condition.split(" gt ");
                String field = parts[0].trim();
                String value = parts[1].replace("'", "").trim();
                employees = employees.stream()
                        .filter(e -> filterUtil.matchComparison(e, field, "gt", value))
                        .collect(Collectors.toList());
            }
        }
        return employees;
    }


    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return (xfHeader == null) ? request.getRemoteAddr() : xfHeader.split(",")[0];
    }
}