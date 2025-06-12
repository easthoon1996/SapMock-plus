package com.dreamsecurity.sapmock.service;

import com.dreamsecurity.sapmock.model.Employee;
import com.dreamsecurity.sapmock.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeQueryService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeQueryService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> findById(String id) {
        return employeeRepository.findById(id);
    }

    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    public boolean hasEnoughEmployees(int required) {
        return employeeRepository.count() >= required;
    }
}
