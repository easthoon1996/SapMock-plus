package com.dreamsecurity.sapmock.component;

import com.dreamsecurity.sapmock.service.EmployeeGenerationService;
import com.dreamsecurity.sapmock.service.EmployeeQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupEmployeeGenerator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupEmployeeGenerator.class);

    private final EmployeeGenerationService employeeGenerationServiceService;
    private final EmployeeQueryService employeeQueryService;

    @Value("${sap.gen.count:50}") // 기본값 50
    private int defaultCount;

    public StartupEmployeeGenerator(EmployeeGenerationService employeeGenerationServiceService, EmployeeQueryService employeeQueryService) {
        this.employeeGenerationServiceService = employeeGenerationServiceService;
        this.employeeQueryService = employeeQueryService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (employeeQueryService.hasEnoughEmployees(defaultCount)) {
            log.info("기존에 직원이 {}명 이상 존재하므로 생성 생략", defaultCount);
            return;
        }

        log.info("SAP Mock 서버 기동 후 직원 {}명 생성 시작", defaultCount);
        employeeGenerationServiceService.generateEmployees(defaultCount);
        log.info("초기 직원 데이터 생성 완료!");
    }
}
