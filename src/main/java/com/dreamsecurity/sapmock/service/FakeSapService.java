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
    private final List<Employee> employees = new ArrayList<>();
    private int nextId = 10000;

    public FakeSapService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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
            emp.setDepartment(faker.number().digits(4));
            emp.setDepartmentName(faker.company().industry());
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
        return employees;
    }

    public Employee addEmployee(Employee newEmp) {
        int maxId = employees.stream()
                .mapToInt(e -> Integer.parseInt(e.getEmployeeId()))
                .max()
                .orElse(nextId);
        newEmp.setEmployeeId(String.valueOf(maxId + 1));
        employees.add(newEmp);
        return newEmp;
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public List<Employee> getEmployees(int skip, int top, String filter) {
        List<Employee> filtered = employees;

        if (filter != null && !filter.isEmpty()) {
            String[] conditions = filter.split(" and ");
            for (String condition : conditions) {
                condition = condition.trim();

                // 🔥 eq 처리
                if (condition.contains(" eq ")) {
                    String[] parts = condition.split(" eq ");
                    String field = parts[0].trim();
                    String value = parts[1].replace("'", "").trim();
                    filtered = filtered.stream()
                            .filter(e -> matchEquals(e, field, value))
                            .collect(Collectors.toList());
                } else {
                    // 🔥 gt/ge/lt/le 처리
                    String operator = null;
                    if (condition.contains(" gt ")) operator = "gt";
                    else if (condition.contains(" ge ")) operator = "ge";
                    else if (condition.contains(" lt ")) operator = "lt";
                    else if (condition.contains(" le ")) operator = "le";

                    // ✅ operator가 있으면 처리
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
