package com.dreamsecurity.sapmock.controller;

import com.dreamsecurity.sapmock.export.CsvExporter;
import com.dreamsecurity.sapmock.model.Employee;
import com.dreamsecurity.sapmock.service.FakeSapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class EmployeeExportController {

    @Autowired
    private CsvExporter csvExporter;

    @Autowired
    private FakeSapService fakeSapService;

    private final RestTemplate restTemplate;

    public EmployeeExportController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/download/employees")
    public void downloadEmployeesCsv(HttpServletResponse response) throws IOException {
        // 1️⃣ CSV 파일명
        String fileName = "employees.csv";

        // 2️⃣ 응답 헤더 설정
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        // 3️⃣ 직원 목록 가져오기
        List<Employee> employees = fakeSapService.generateEmployees(20); // 예시: 20명 생성

        // 4️⃣ CsvExporter를 직접 응답에 쓰도록 수정
        csvExporter.exportEmployeesToResponse(employees, response);
    }
}
