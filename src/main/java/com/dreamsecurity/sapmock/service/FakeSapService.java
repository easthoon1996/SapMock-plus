package com.dreamsecurity.sapmock.service;

import com.dreamsecurity.sapmock.model.Employee;
import com.github.javafaker.Faker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FakeSapService {
    private static final Faker faker = new Faker(new Locale("ko"));

    private final RestTemplate restTemplate;

    public FakeSapService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 클래스 레벨에서 employees 선언
    private final List<Employee> employees = new ArrayList<>();
    private int nextId = 10000; // 초기값 (예: 10000)

    // 가상의 SAP 인사정보 데이터 생성
    public List<Employee> generateEmployees(int count) {
        employees.clear(); // 기존 데이터 초기화

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
            emp.setDepartment(faker.number().digits(4));
            emp.setDepartmentName(faker.company().industry());
            emp.setHireDate(toLocalDate(faker.date().past(5000, TimeUnit.DAYS)));
            emp.setTerminationDate(faker.bool().bool() ? toLocalDate(faker.date().future(1000, TimeUnit.DAYS)) : null);

            // 영어 이름 기반 이메일 생성
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

        return employees;
    }

    // 신규 사용자 추가
    public Employee addEmployee(Employee newEmp) {
        // 현재 employees에서 가장 큰 employeeId 찾기
        int maxId = employees.stream()
                .mapToInt(e -> Integer.parseInt(e.getEmployeeId()))
                .max()
                .orElse(nextId); // 만약 employees 비어있으면 10000부터 시작

        // 새로운 ID로 지정
        newEmp.setEmployeeId(String.valueOf(maxId + 1));
        employees.add(newEmp);
        return newEmp;
    }

    // Date → LocalDate 변환
    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 페이징 & 증분 조회 지원
     *
     * @param skip           시작 인덱스
     * @param top            조회 개수
     * @param hireDateAfter  입사일자 이후 조건 (YYYY-MM-DD)
     * @return 필터링된 직원 리스트
     */
    public List<Employee> getEmployees(int skip, int top, String hireDateAfter) {
        List<Employee> filtered = employees;

        // 증분 조회 (입사일 이후)
        if (hireDateAfter != null && !hireDateAfter.isEmpty()) {
            LocalDate hireDateFilter = LocalDate.parse(hireDateAfter);
            filtered = filtered.stream()
                    .filter(e -> e.getHireDate().isAfter(hireDateFilter))
                    .collect(Collectors.toList());
        }

        // 페이징 처리
        int end = Math.min(skip + top, filtered.size());
        if (skip >= filtered.size()) {
            return Collections.emptyList();
        }
        return filtered.subList(skip, end);
    }

    public Optional<Employee> findEmployeeById(String employeeId) {
        return employees.stream()
                .filter(e -> e.getEmployeeId().equals(employeeId))
                .findFirst();
    }


}
