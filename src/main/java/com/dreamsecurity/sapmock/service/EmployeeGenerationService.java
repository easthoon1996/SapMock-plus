package com.dreamsecurity.sapmock.service;

import com.dreamsecurity.sapmock.model.Employee;
import com.dreamsecurity.sapmock.model.Role;
import com.dreamsecurity.sapmock.repository.EmployeeRepository;
import com.dreamsecurity.sapmock.repository.RoleRepository;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class EmployeeGenerationService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final Faker faker = new Faker(new Locale("ko"));

    @Autowired
    public EmployeeGenerationService(EmployeeRepository employeeRepository, RoleRepository roleRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
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

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}