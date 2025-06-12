package com.dreamsecurity.sapmock.service;

import com.dreamsecurity.sapmock.controller.EmployeeController;
import com.dreamsecurity.sapmock.model.Employee;
import com.dreamsecurity.sapmock.model.Privilege;
import com.dreamsecurity.sapmock.model.Role;
import com.dreamsecurity.sapmock.model.AuthorizationObject;
import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FakeSapService {

    private static final Logger log = LoggerFactory.getLogger(FakeSapService.class);

    private static final Faker faker = new Faker(new Locale("ko"));
    private final RestTemplate restTemplate;
    private final List<Employee> employees = new ArrayList<>();

    // SAP의 권한 객체 마스터 (AuthorizationObject)
    private final List<AuthorizationObject> masterAuthObjects = List.of(
            new AuthorizationObject("S_USER_GRP", "사용자 그룹 관리", List.of("ACTVT")),
            new AuthorizationObject("S_TCODE", "트랜잭션 코드 실행", List.of("TCD")),
            new AuthorizationObject("S_PROGRAM", "프로그램 실행", List.of("ACTVT")),
            new AuthorizationObject("S_DATASET", "파일 액세스", List.of("ACTVT", "FILENAME"))
    );

    // SAP의 권한 객체별 권한 상세 (Privilege)
    private final Map<String, List<Privilege>> masterPrivileges = Map.of(
            "S_USER_GRP", List.of(
                    new Privilege("S_USER_GRP", "ACTVT=01", "사용자 그룹 생성"),
                    new Privilege("S_USER_GRP", "ACTVT=02", "사용자 그룹 수정"),
                    new Privilege("S_USER_GRP", "ACTVT=03", "사용자 그룹 조회")
            ),
            "S_TCODE", List.of(
                    new Privilege("S_TCODE", "TCD=SM30", "테이블 유지관리 실행"),
                    new Privilege("S_TCODE", "TCD=SE38", "ABAP 프로그램 실행"),
                    new Privilege("S_TCODE", "TCD=VA01", "판매 주문 생성")
            ),
            "S_PROGRAM", List.of(
                    new Privilege("S_PROGRAM", "ACTVT=03", "프로그램 조회")
            )
    );

    // 역할(Role) 마스터 정의
    private final List<Role> masterRoles = List.of(
            new Role("ADMIN", "시스템 관리자", "SAP 시스템 전체 관리", combinePrivileges(List.of("S_USER_GRP", "S_TCODE"))),
            new Role("DEVELOPER", "개발자", "SAP 개발자 권한", combinePrivileges(List.of("S_TCODE", "S_PROGRAM"))),
            new Role("SALES", "영업 담당자", "SAP 영업 기능 접근", combinePrivileges(List.of("S_TCODE")))
    );

    public FakeSapService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 직원 생성 (SAP 실제처럼 역할/권한 기반)
     */
    public List<Employee> generateEmployees(int count) {
        employees.clear();
        for (int i = 1; i <= count; i++) {
            Employee emp = new Employee();
            emp.setEmployeeId(String.format("%05d", 10000 + i));
            emp.setFirstName(faker.name().firstName());
            emp.setLastName(faker.name().lastName());
            emp.setMiddleName(faker.bool().bool() ? faker.name().firstName() : "");
            emp.setBirthDate(toLocalDate(faker.date().birthday(20, 60)));
            emp.setGender(faker.demographic().sex().substring(0, 1).toUpperCase());
            emp.setNationality(faker.country().countryCode2());
            emp.setMaritalStatus(faker.options().option("Single", "Married", "Divorced", "Widowed"));
            emp.setPosition(faker.job().position());
            emp.setJobTitle(faker.job().title());

            // 부서 기반으로 역할 배정
            String deptType = faker.options().option("IT", "Sales", "Admin");
            emp.setDepartment(deptType.equals("IT") ? "1001" : deptType.equals("Sales") ? "2001" : "0001");
            emp.setDepartmentName(deptType);

            emp.setHireDate(toLocalDate(faker.date().past(5000, TimeUnit.DAYS)));
            emp.setTerminationDate(faker.bool().bool() ? toLocalDate(faker.date().future(1000, TimeUnit.DAYS)) : null);

            Faker englishFaker = new Faker(new Locale("en"));
            String englishFirstName = englishFaker.name().firstName().toLowerCase();
            String englishLastName = englishFaker.name().lastName().toLowerCase();
            emp.setWorkEmail(englishFirstName + "." + englishLastName + "@company.com");

            emp.setWorkPhone(faker.phoneNumber().phoneNumber());
            emp.setMobilePhone(faker.phoneNumber().cellPhone());
            emp.setAddress(faker.address().fullAddress());
            emp.setBankAccount(faker.number().digits(12));
            emp.setTaxId(faker.number().digits(9));

            // 역할 배정 (부서 기반)
            if (deptType.equals("IT")) {
                emp.getRoles().add(cloneRoleById("DEVELOPER"));
            } else if (deptType.equals("Sales")) {
                emp.getRoles().add(cloneRoleById("SALES"));
            } else {
                emp.getRoles().add(cloneRoleById("ADMIN"));
            }

            // 필요에 따라 추가 역할을 랜덤으로 부여 (1명이 여러 Role을 가질 수 있도록)
            if (faker.bool().bool()) {
                emp.getRoles().add(cloneRoleById("ADMIN"));
            }

            employees.add(emp);
        }
        return employees;
    }

    /**
     * 역할 복사
     */
    private Role cloneRoleById(String roleId) {
        return masterRoles.stream()
                .filter(r -> r.getRoleId().equals(roleId))
                .findFirst()
                .map(r -> {
                    Role copy = new Role();
                    copy.setRoleId(r.getRoleId());
                    copy.setRoleName(r.getRoleName());
                    copy.setDescription(r.getDescription());
                    for (Privilege p : r.getPrivileges()) {
                        copy.getPrivileges().add(new Privilege(p.getPrivilegeId(), p.getPrivilegeName(), p.getDescription()));
                    }
                    return copy;
                })
                .orElse(null);
    }

    /**
     * 권한 객체별 Privilege를 복사해서 Role에 넣기
     */
    private static List<Privilege> combinePrivileges(List<String> authObjectIds) {
        List<Privilege> combined = new ArrayList<>();
        for (String authId : authObjectIds) {
            combined.addAll(
                    masterPrivilegesStatic().getOrDefault(authId, Collections.emptyList()).stream()
                            .map(p -> new Privilege(p.getPrivilegeId(), p.getPrivilegeName(), p.getDescription()))
                            .collect(Collectors.toList())
            );
        }
        return combined;
    }

    // static으로 masterPrivileges를 재사용 (combinePrivileges에서 필요)
    private static Map<String, List<Privilege>> masterPrivilegesStatic() {
        return Map.of(
                "S_USER_GRP", List.of(
                        new Privilege("S_USER_GRP", "ACTVT=01", "사용자 그룹 생성"),
                        new Privilege("S_USER_GRP", "ACTVT=02", "사용자 그룹 수정"),
                        new Privilege("S_USER_GRP", "ACTVT=03", "사용자 그룹 조회")
                ),
                "S_TCODE", List.of(
                        new Privilege("S_TCODE", "TCD=SM30", "테이블 유지관리 실행"),
                        new Privilege("S_TCODE", "TCD=SE38", "ABAP 프로그램 실행"),
                        new Privilege("S_TCODE", "TCD=VA01", "판매 주문 생성")
                ),
                "S_PROGRAM", List.of(
                        new Privilege("S_PROGRAM", "ACTVT=03", "프로그램 조회")
                )
        );
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 신규 직원 등록
     */
    public Employee addEmployee(Employee newEmp) {
        // 🔥 employeeId를 새로 지정 (최대값 +1)
        int maxId = employees.stream()
                .mapToInt(e -> Integer.parseInt(e.getEmployeeId()))
                .max()
                .orElse(10000); // 시작 ID

        newEmp.setEmployeeId(String.format("%05d", maxId + 1));

        // 🚀 직원에게도 최소한 1개의 역할을 랜덤으로 배정 (부서 기반으로)
        if (newEmp.getDepartmentName() != null) {
            switch (newEmp.getDepartmentName().toLowerCase()) {
                case "it":
                    newEmp.getRoles().add(cloneRoleById("DEVELOPER"));
                    break;
                case "sales":
                    newEmp.getRoles().add(cloneRoleById("SALES"));
                    break;
                default:
                    newEmp.getRoles().add(cloneRoleById("ADMIN"));
                    break;
            }
        } else {
            // 부서명이 없으면 기본적으로 ADMIN 부여
            newEmp.getRoles().add(cloneRoleById("ADMIN"));
        }

        employees.add(newEmp);
        return newEmp;
    }

    public List<Employee> getEmployees(int skip, int top, String filter) {
        List<Employee> filtered = employees;

        if (filter != null && !filter.isEmpty()) {
            String[] conditions = filter.split(" and ");
            for (String condition : conditions) {
                condition = condition.trim();

                // eq 처리
                if (condition.contains(" eq ")) {
                    String[] parts = condition.split(" eq ");
                    String field = parts[0].trim();
                    String value = parts[1].replace("'", "").trim();
                    filtered = filtered.stream()
                            .filter(e -> matchEquals(e, field, value))
                            .collect(Collectors.toList());
                } else {
                    // gt/ge/lt/le 처리
                    String operator = null;
                    if (condition.contains(" gt ")) operator = "gt";
                    else if (condition.contains(" ge ")) operator = "ge";
                    else if (condition.contains(" lt ")) operator = "lt";
                    else if (condition.contains(" le ")) operator = "le";

                    // operator가 있으면 처리
                    if (operator != null) {
                        String[] parts = condition.split(" " + operator + " ");
                        if (parts.length == 2) {
                            String field = parts[0].trim();
                            String value = parts[1].replace("'", "").trim();

                            String finalOperator = operator; // 람다에서 effectively final
                            filtered = filtered.stream()
                                    .filter(e -> matchComparison(e, field, finalOperator, value))
                                    .collect(Collectors.toList());
                        }
                    }
                }
            }
        }


        int end = Math.min(skip + top, filtered.size());
        if (skip >= filtered.size()) {
            return Collections.emptyList();
        }
        return filtered.subList(skip, end);
    }

    private boolean matchEquals(Employee e, String field, String value) {
        switch (field) {
            case "employeeId": return e.getEmployeeId().equals(value);
            case "firstName": return e.getFirstName().equalsIgnoreCase(value);
            case "lastName": return e.getLastName().equalsIgnoreCase(value);
            case "middleName": return e.getMiddleName().equalsIgnoreCase(value);
            case "gender": return e.getGender().equalsIgnoreCase(value);
            case "nationality": return e.getNationality().equalsIgnoreCase(value);
            case "maritalStatus": return e.getMaritalStatus().equalsIgnoreCase(value);
            case "position": return e.getPosition().equalsIgnoreCase(value);
            case "jobTitle": return e.getJobTitle().equalsIgnoreCase(value);
            case "department": return e.getDepartment().equals(value);
            case "departmentName": return e.getDepartmentName().equalsIgnoreCase(value);
            case "workEmail": return e.getWorkEmail().equalsIgnoreCase(value);
            case "workPhone": return e.getWorkPhone().equalsIgnoreCase(value);
            case "mobilePhone": return e.getMobilePhone().equalsIgnoreCase(value);
            case "address": return e.getAddress().equalsIgnoreCase(value);
            case "bankAccount": return e.getBankAccount().equals(value);
            case "taxId": return e.getTaxId().equals(value);
            case "birthDate":
                return e.getBirthDate() != null && e.getBirthDate().toString().equals(value);
            case "hireDate":
                return e.getHireDate() != null && e.getHireDate().toString().equals(value);
            case "terminationDate":
                return e.getTerminationDate() != null && e.getTerminationDate().toString().equals(value);
            default:
                return false;
        }
    }

    private boolean matchComparison(Employee e, String field, String operator, String value) {
        switch (field) {
            case "birthDate":
                return compareDates(e.getBirthDate(), operator, value);
            case "hireDate":
                return compareDates(e.getHireDate(), operator, value);
            case "terminationDate":
                return compareDates(e.getTerminationDate(), operator, value);
            case "department":
            case "bankAccount":
            case "taxId":
                return compareNumbers(getFieldValueAsString(e, field), operator, value);
            default:
                return false; // 문자열 비교는 eq만 지원
        }
    }

    private boolean compareDates(LocalDate date, String operator, String value) {
        if (date == null) return false;
        LocalDate target = LocalDate.parse(value);
        switch (operator) {
            case "gt": return date.isAfter(target);
            case "ge": return !date.isBefore(target);
            case "lt": return date.isBefore(target);
            case "le": return !date.isAfter(target);
            default: return false;
        }
    }

    private boolean compareNumbers(String fieldValue, String operator, String value) {
        try {
            long fieldNum = Long.parseLong(fieldValue);
            long targetNum = Long.parseLong(value);
            switch (operator) {
                case "gt": return fieldNum > targetNum;
                case "ge": return fieldNum >= targetNum;
                case "lt": return fieldNum < targetNum;
                case "le": return fieldNum <= targetNum;
                default: return false;
            }
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String getFieldValueAsString(Employee e, String field) {
        switch (field) {
            case "department": return e.getDepartment();
            case "bankAccount": return e.getBankAccount();
            case "taxId": return e.getTaxId();
            default: return "";
        }
    }

    public Optional<Employee> findEmployeeById(String employeeId) {
        return employees.stream()
                .filter(e -> e.getEmployeeId().equals(employeeId))
                .findFirst();
    }

}
