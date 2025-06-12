package com.dreamsecurity.sapmock.service;

import com.dreamsecurity.sapmock.model.Employee;
import com.dreamsecurity.sapmock.model.Privilege;
import com.dreamsecurity.sapmock.model.Role;
import com.dreamsecurity.sapmock.repository.EmployeeRepository;
import com.dreamsecurity.sapmock.repository.PrivilegeRepository;
import com.dreamsecurity.sapmock.repository.RoleRepository;
import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FakeSapService {

    private static final Logger log = LoggerFactory.getLogger(FakeSapService.class);
    private static final Faker faker = new Faker(new Locale("ko"));

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @PostConstruct
    @Transactional
    public void initMasterData() {
        log.info("[initMasterData] 마스터 데이터 초기화 시작");

        List<Privilege> privileges = List.of(
                new Privilege("S_USER_GRP", "ACTVT=01", "사용자 그룹 생성"),
                new Privilege("S_USER_GRP", "ACTVT=02", "사용자 그룹 수정"),
                new Privilege("S_USER_GRP", "ACTVT=03", "사용자 그룹 조회"),
                new Privilege("S_TCODE", "TCD=SM30", "테이블 유지관리 실행"),
                new Privilege("S_TCODE", "TCD=SE38", "ABAP 프로그램 실행"),
                new Privilege("S_TCODE", "TCD=VA01", "판매 주문 생성"),
                new Privilege("S_PROGRAM", "ACTVT=03", "프로그램 조회"),
                new Privilege("S_USER_AUTH", "ACTVT=01", "권한 부여"),
                new Privilege("S_DEVELOP", "DEV=ALL", "개발 권한"),
                new Privilege("S_TRANSPRT", "TR=ALL", "운송 권한"),
                new Privilege("VA_VBAK_VBK", "SALES=ALL", "영업 오더 보기"),
                new Privilege("SD_VBAK_AAT", "SALES=CHANGE", "영업 오더 변경"),
                new Privilege("S_RFC", "RFC=ALL", "RFC 접근"),
                new Privilege("S_DATASET", "FILE=ALL", "파일 접근"),
                new Privilege("P_ORGIN", "HR=ALL", "인사 정보 접근"),
                new Privilege("M_MATE_MAT", "MAT=ALL", "자재 정보 접근"),
                new Privilege("MM_PUR_PO", "PURCHASE=PO", "구매 오더"),
                new Privilege("MM_PUR_PR", "PURCHASE=PR", "구매 요청"),
                new Privilege("F_BKPF_BUK", "FI=ALL", "재무 데이터 접근"),
                new Privilege("FI_GL_ACC", "GL=ALL", "일반원장 접근"),
                new Privilege("K_KOSTL", "CO=ALL", "관리회계 접근"),
                new Privilege("CO_CCTR", "CCTR=ALL", "원가센터 접근"),
                new Privilege("PP_ORDER", "PP=ALL", "생산 오더 접근"),
                new Privilege("QM_QINFO", "QM=ALL", "품질 정보 접근"),
                new Privilege("PM_EQUI", "PM=ALL", "설비 정보 접근"),
                new Privilege("WM_LQUA", "WM=ALL", "재고 정보 접근"),
                new Privilege("SD_BILLING", "SD=ALL", "청구 관리 접근"),
                new Privilege("BW_REPORT", "BW=ALL", "BI 보고서 접근"),
                new Privilege("APO_PLAN", "APO=ALL", "계획 접근"),
                new Privilege("IT_SEC", "IT=SEC", "보안 관리"),
                new Privilege("IT_MON", "IT=MON", "모니터링"),
                new Privilege("IT_CFG", "IT=CFG", "시스템 설정"),
                new Privilege("GRC_ACCESS", "GRC=ALL", "GRC 접근 제어"),
                new Privilege("SOLMAN_MON", "SOLMAN=ALL", "솔루션 매니저 모니터링"),
                new Privilege("S4HANA_CORE", "S4=CORE", "S/4HANA 기본 권한")
        );

        privileges.forEach(p -> {
            if (!privilegeRepository.existsByPrivilegeIdAndPrivilegeName(p.getPrivilegeId(), p.getPrivilegeName())) {
                privilegeRepository.save(p);
            }
        });

        Map<String, List<Privilege>> privMap = privileges.stream()
                .collect(Collectors.groupingBy(Privilege::getPrivilegeId));

        List<Role> roles = List.of(
                new Role("ADMIN", "시스템 관리자", "SAP 시스템 전체 관리", combinePrivileges(privMap, "S_USER_GRP", "S_TCODE", "S_USER_AUTH")),
                new Role("DEVELOPER", "개발자", "SAP 개발자 권한", combinePrivileges(privMap, "S_TCODE", "S_PROGRAM", "S_DEVELOP", "S_TRANSPRT")),
                new Role("SALES", "영업 담당자", "SAP 영업 기능 접근", combinePrivileges(privMap, "S_TCODE", "VA_VBAK_VBK", "SD_VBAK_AAT")),
                new Role("BASIS", "기술 관리자", "시스템 기술 지원", combinePrivileges(privMap, "S_RFC", "S_DATASET")),
                new Role("FILE_ADMIN", "파일 관리자", "파일 업로드/다운로드 권한", combinePrivileges(privMap, "S_DATASET")),
                new Role("HR", "인사 관리자", "SAP 인사 관리 기능", combinePrivileges(privMap, "P_ORGIN")),
                new Role("MM", "자재 관리자", "SAP 자재 관리 기능", combinePrivileges(privMap, "M_MATE_MAT", "MM_PUR_PO", "MM_PUR_PR")),
                new Role("FI", "재무 관리자", "SAP 재무 회계 기능", combinePrivileges(privMap, "F_BKPF_BUK", "FI_GL_ACC")),
                new Role("CO", "관리회계 관리자", "SAP 관리회계 기능", combinePrivileges(privMap, "K_KOSTL", "CO_CCTR")),
                new Role("PP", "생산 관리자", "SAP 생산 관리 기능", combinePrivileges(privMap, "PP_ORDER")),
                new Role("QM", "품질 관리자", "SAP 품질 관리 기능", combinePrivileges(privMap, "QM_QINFO")),
                new Role("PM", "설비 관리자", "SAP 설비 관리 기능", combinePrivileges(privMap, "PM_EQUI")),
                new Role("WM", "창고 관리자", "SAP 창고 관리 기능", combinePrivileges(privMap, "WM_LQUA")),
                new Role("SD", "판매 관리자", "SAP SD 모듈 기능", combinePrivileges(privMap, "SD_VBAK_AAT", "SD_BILLING")),
                new Role("BW", "BI 관리자", "SAP BW 분석 보고서 기능", combinePrivileges(privMap, "BW_REPORT")),
                new Role("APO", "계획 관리자", "SAP APO 계획 기능", combinePrivileges(privMap, "APO_PLAN")),
                new Role("IT_ADMIN", "IT 관리자", "SAP 시스템 IT 설정 및 모니터링", combinePrivileges(privMap, "IT_SEC", "IT_MON", "IT_CFG")),
                new Role("GRC", "접근 통제 관리자", "GRC 접근 권한 감사 및 분석", combinePrivileges(privMap, "GRC_ACCESS")),
                new Role("SOLMAN", "솔루션 매니저 관리자", "SAP 솔루션 매니저 기능", combinePrivileges(privMap, "SOLMAN_MON")),
                new Role("S4HANA", "S/4HANA 사용자", "SAP S/4HANA 핵심 기능", combinePrivileges(privMap, "S4HANA_CORE"))
        );

        roleRepository.saveAll(roles);
    }

    @Transactional
    public void generateEmployees(int count) {
        List<Role> roles = roleRepository.findAll();
        if (roles.isEmpty()) {
            throw new IllegalStateException("역할(Role) 정보가 존재하지 않습니다. 마스터 데이터를 먼저 초기화하세요.");
        }

        Map<String, List<String>> deptRoleMap = Map.of(
                "IT", List.of("ADMIN", "DEVELOPER", "BASIS", "IT_ADMIN", "S4HANA"),
                "Sales", List.of("SALES", "SD", "BW"),
                "Admin", List.of("HR", "FILE_ADMIN", "GRC", "SOLMAN"),
                "Logistics", List.of("MM", "WM", "PM", "PP", "QM"),
                "Finance", List.of("FI", "CO")
        );

        List<String> departments = new ArrayList<>(deptRoleMap.keySet());
        List<Employee> employees = new ArrayList<>();

        Map<String, Role> roleMap = roles.stream().collect(Collectors.toMap(Role::getRoleId, r -> r));

        for (int i = 0; i < count; i++) {
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

            String deptType = faker.options().option(departments.toArray(new String[0]));
            emp.setDepartment(
                    deptType.equals("IT") ? "1001" :
                            deptType.equals("Sales") ? "2001" :
                                    deptType.equals("Finance") ? "3001" :
                                            deptType.equals("Logistics") ? "4001" : "0001");
            emp.setDepartmentName(deptType);

            List<String> roleIds = deptRoleMap.getOrDefault(deptType, List.of());
            Set<Role> assignedRoles = new HashSet<>();
            while (assignedRoles.isEmpty()) {
                int numRoles = faker.number().numberBetween(1, 4); // 최소 1개 보장
                Collections.shuffle(roles);
                assignedRoles.addAll(roles.subList(0, numRoles));
            }
            emp.setRoles(assignedRoles);

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

            employees.add(emp);
        }
        employeeRepository.deleteAll();
        employeeRepository.saveAll(employees);
    }

    public Employee saveEmployee(Employee emp) {
        return employeeRepository.save(emp);
    }

    @Transactional(readOnly = true)
    public List<Employee> findAllEmployees(int skip, int top, String filter) {
        List<Employee> all = employeeRepository.findAll();

        if (filter != null && !filter.isBlank()) {
            String[] conditions = filter.split(" and ");
            for (String condition : conditions) {
                condition = condition.trim();
                if (condition.contains(" eq ")) {
                    String[] parts = condition.split(" eq ");
                    String field = parts[0].trim();
                    String value = parts[1].replace("'", "").trim();

                    if (field.equals("roleId")) {
                        all = all.stream()
                                .filter(e -> e.getRoles().stream().anyMatch(r -> r.getRoleId().equalsIgnoreCase(value)))
                                .collect(Collectors.toList());
                    } else if (field.equals("privilegeId")) {
                        all = all.stream()
                                .filter(e -> e.getRoles().stream()
                                        .flatMap(r -> r.getPrivileges().stream())
                                        .anyMatch(p -> p.getPrivilegeId().equalsIgnoreCase(value)))
                                .collect(Collectors.toList());
                    } else {
                        all = all.stream().filter(e -> matchEquals(e, field, value)).collect(Collectors.toList());
                    }
                } else {
                    String operator;
                    if (condition.contains(" gt ")) operator = "gt";
                    else if (condition.contains(" ge ")) operator = "ge";
                    else if (condition.contains(" lt ")) operator = "lt";
                    else if (condition.contains(" le ")) operator = "le";
                    else {
                        operator = null;
                    }

                    if (operator != null) {
                        String[] parts = condition.split(" " + operator + " ");
                        if (parts.length == 2) {
                            String field = parts[0].trim();
                            String value = parts[1].replace("'", "").trim();
                            all = all.stream().filter(e -> matchComparison(e, field, operator, value)).collect(Collectors.toList());
                        }
                    }
                }
            }
        }

        return all.stream().skip(skip).limit(top).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Employee> findEmployeeById(String employeeId) {
        return employeeRepository.findById(employeeId);
    }

    private List<Privilege> combinePrivileges(Map<String, List<Privilege>> map, String... keys) {
        return Arrays.stream(keys)
                .flatMap(k -> map.getOrDefault(k, Collections.emptyList()).stream())
                .collect(Collectors.toList());
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
            case "birthDate": return e.getBirthDate() != null && e.getBirthDate().toString().equals(value);
            case "hireDate": return e.getHireDate() != null && e.getHireDate().toString().equals(value);
            case "terminationDate": return e.getTerminationDate() != null && e.getTerminationDate().toString().equals(value);
            default: return false;
        }
    }

    private boolean matchComparison(Employee e, String field, String operator, String value) {
        switch (field) {
            case "birthDate": return compareDates(e.getBirthDate(), operator, value);
            case "hireDate": return compareDates(e.getHireDate(), operator, value);
            case "terminationDate": return compareDates(e.getTerminationDate(), operator, value);
            case "department":
            case "bankAccount":
            case "taxId": return compareNumbers(getFieldValueAsString(e, field), operator, value);
            default: return false;
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

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public boolean hasEnoughEmployees(int requiredCount) {
        return employeeRepository.count() >= requiredCount;
    }

    public List<Employee> findByRoleId(String roleId) {
        return employeeRepository.findAll().stream()
                .filter(emp -> emp.getRoles().stream().anyMatch(role -> role.getRoleId().equalsIgnoreCase(roleId)))
                .collect(Collectors.toList());
    }

    public List<Employee> findByPrivilegeId(String privilegeId) {
        return employeeRepository.findAll().stream()
                .filter(emp -> emp.getRoles().stream()
                        .flatMap(role -> role.getPrivileges().stream())
                        .anyMatch(priv -> priv.getPrivilegeId().equalsIgnoreCase(privilegeId)))
                .collect(Collectors.toList());
    }



}
