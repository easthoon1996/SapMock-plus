package com.dreamsecurity.sapmock.export;

import com.dreamsecurity.sapmock.model.Employee;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@Service
public class CsvExporter {

    public void exportEmployeesToCsv(List<Employee> employees, String filePath) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(filePath),
                CSVFormat.DEFAULT.withHeader("employeeId", "firstName", "lastName", "middleName", "birthDate", "gender",
                        "nationality", "maritalStatus", "position", "jobTitle", "department", "departmentName",
                        "hireDate", "terminationDate", "workEmail", "workPhone", "mobilePhone", "address",
                        "bankAccount", "taxId"))) {

            for (Employee e : employees) {
                printer.printRecord(e.getEmployeeId(), e.getFirstName(), e.getLastName(), e.getMiddleName(),
                        e.getBirthDate(), e.getGender(), e.getNationality(), e.getMaritalStatus(), e.getPosition(),
                        e.getJobTitle(), e.getDepartment(), e.getDepartmentName(), e.getHireDate(), e.getTerminationDate(),
                        e.getWorkEmail(), e.getWorkPhone(), e.getMobilePhone(), e.getAddress(),
                        e.getBankAccount(), e.getTaxId());
            }
        }
    }

    // 🔥 추가: OutputStream으로 바로 쓰는 메서드
    public void exportEmployeesToResponse(List<Employee> employees, HttpServletResponse response) throws IOException {
        // 👉 OutputStreamWriter를 UTF-8로 명시적으로 지정
        response.setContentType("text/csv; charset=UTF-8");

        // 2️⃣ OutputStream 가져오기
        OutputStream out = response.getOutputStream();

        // 3️⃣ BOM 추가
        out.write(0xEF);
        out.write(0xBB);
        out.write(0xBF);

        try (CSVPrinter printer = new CSVPrinter(new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8")),
                CSVFormat.DEFAULT.withHeader("employeeId", "firstName", "lastName", "middleName", "birthDate", "gender",
                        "nationality", "maritalStatus", "position", "jobTitle", "department", "departmentName",
                        "hireDate", "terminationDate", "workEmail", "workPhone", "mobilePhone", "address",
                        "bankAccount", "taxId"))) {

            for (Employee e : employees) {
                printer.printRecord(
                        e.getEmployeeId(),
                        e.getFirstName(),
                        e.getLastName(),
                        e.getMiddleName(),
                        e.getBirthDate(),
                        e.getGender(),
                        e.getNationality(),
                        e.getMaritalStatus(),
                        e.getPosition(),
                        e.getJobTitle(),
                        e.getDepartment(),
                        e.getDepartmentName(),
                        e.getHireDate(),
                        e.getTerminationDate(),
                        e.getWorkEmail(),
                        e.getWorkPhone(),
                        e.getMobilePhone(),
                        e.getAddress(),
                        "=\"" + e.getBankAccount() + "\"",
                        "=\"" + e.getTaxId() + "\""
                );
            }
        }
    }

}
