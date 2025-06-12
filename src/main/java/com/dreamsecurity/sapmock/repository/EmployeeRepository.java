package com.dreamsecurity.sapmock.repository;

import com.dreamsecurity.sapmock.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
}