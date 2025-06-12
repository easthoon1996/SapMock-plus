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

        Map<String, List<String>> deptRoleMap = Map.of(
                "IT", List.of("ADMIN", "DEVELOPER"),
                "Admin", List.of("HR")
        );

        List<String> departments = new ArrayList<>(deptRoleMap.keySet());
        List<Employee> employees = new ArrayList<>();
        Map<String, Role> roleMap = roles.stream().collect(Collectors.toMap(Role::getRoleId, r -> r));

        for (int i = 0; i < count; i++) {
            Employee emp = new Employee();
            emp.setEmployeeId(String.format("%05d", 10000 + i));
            emp.setFirstName(faker.name().firstName());
            emp.setLastName(faker.name().lastName());
            emp.setGender(faker.demographic().sex().substring(0, 1).toUpperCase());
            emp.setDepartmentName(faker.options().option(departments.toArray(new String[0])));
            emp.setDepartment("0001");
            emp.setHireDate(toLocalDate(faker.date().past(5000, TimeUnit.DAYS)));
            emp.setWorkEmail("employee" + i + "@company.com");

            Set<Role> assignedRoles = new HashSet<>();
            Collections.shuffle(roles);
            assignedRoles.add(roles.get(0));
            emp.setRoles(assignedRoles);

            employees.add(emp);
        }

        employeeRepository.deleteAll();
        employeeRepository.saveAll(employees);
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}