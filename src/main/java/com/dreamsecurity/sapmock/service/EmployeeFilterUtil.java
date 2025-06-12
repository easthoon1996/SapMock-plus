package com.dreamsecurity.sapmock.service;

import com.dreamsecurity.sapmock.model.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EmployeeFilterUtil {

    public boolean matchEquals(Employee e, String field, String value) {
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

    public boolean matchComparison(Employee e, String field, String operator, String value) {
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
}
