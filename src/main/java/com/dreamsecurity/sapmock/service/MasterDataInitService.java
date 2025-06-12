package com.dreamsecurity.sapmock.service;

import com.dreamsecurity.sapmock.model.Privilege;
import com.dreamsecurity.sapmock.model.Role;
import com.dreamsecurity.sapmock.repository.PrivilegeRepository;
import com.dreamsecurity.sapmock.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MasterDataInitService {

    private static final Logger log = LoggerFactory.getLogger(MasterDataInitService.class);

    private final PrivilegeRepository privilegeRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public MasterDataInitService(PrivilegeRepository privilegeRepository, RoleRepository roleRepository) {
        this.privilegeRepository = privilegeRepository;
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    @Transactional
    public void initMasterData() {
        log.info("[initMasterData] 마스터 데이터 초기화 시작");

        List<Privilege> privileges = List.of(
                new Privilege("S_USER_GRP", "ACTVT=01", "사용자 그룹 생성"),
                new Privilege("S_USER_GRP", "ACTVT=02", "사용자 그룹 수정"),
                new Privilege("S_USER_GRP", "ACTVT=03", "사용자 그룹 조회"),
                new Privilege("S_TCODE", "TCD=SM30", "테이블 유지관리 실행"),
                new Privilege("S_TCODE", "TCD=SE38", "ABAP 프로그램 실행"),
                new Privilege("S_TCODE", "TCD=VA01", "판매 주문 생성"),
                new Privilege("S_PROGRAM", "ACTVT=03", "프로그램 조회"),
                new Privilege("S_USER_AUTH", "ACTVT=01", "권한 부여"),
                new Privilege("S_DEVELOP", "DEV=ALL", "개발 권한"),
                new Privilege("S_TRANSPRT", "TR=ALL", "운송 권한"),
                new Privilege("VA_VBAK_VBK", "SALES=ALL", "영업 오더 보기"),
                new Privilege("SD_VBAK_AAT", "SALES=CHANGE", "영업 오더 변경"),
                new Privilege("S_RFC", "RFC=ALL", "RFC 접근"),
                new Privilege("S_DATASET", "FILE=ALL", "파일 접근"),
                new Privilege("P_ORGIN", "HR=ALL", "인사 정보 접근"),
                new Privilege("M_MATE_MAT", "MAT=ALL", "자재 정보 접근"),
                new Privilege("MM_PUR_PO", "PURCHASE=PO", "구매 오더"),
                new Privilege("MM_PUR_PR", "PURCHASE=PR", "구매 요청"),
                new Privilege("F_BKPF_BUK", "FI=ALL", "재무 데이터 접근"),
                new Privilege("FI_GL_ACC", "GL=ALL", "일반원장 접근"),
                new Privilege("K_KOSTL", "CO=ALL", "관리회계 접근"),
                new Privilege("CO_CCTR", "CCTR=ALL", "원가센터 접근"),
                new Privilege("PP_ORDER", "PP=ALL", "생산 오더 접근"),
                new Privilege("QM_QINFO", "QM=ALL", "품질 정보 접근"),
                new Privilege("PM_EQUI", "PM=ALL", "설비 정보 접근"),
                new Privilege("WM_LQUA", "WM=ALL", "재고 정보 접근"),
                new Privilege("SD_BILLING", "SD=ALL", "청구 관리 접근"),
                new Privilege("BW_REPORT", "BW=ALL", "BI 보고서 접근"),
                new Privilege("APO_PLAN", "APO=ALL", "계획 접근"),
                new Privilege("IT_SEC", "IT=SEC", "보안 관리"),
                new Privilege("IT_MON", "IT=MON", "모니터링"),
                new Privilege("IT_CFG", "IT=CFG", "시스템 설정"),
                new Privilege("GRC_ACCESS", "GRC=ALL", "GRC 접근 제어"),
                new Privilege("SOLMAN_MON", "SOLMAN=ALL", "솔루션 매니저 모니터링"),
                new Privilege("S4HANA_CORE", "S4=CORE", "S/4HANA 기본 권한")
                // 필요한 권한 계속 추가 가능
        );

        privileges.forEach(p -> {
            if (!privilegeRepository.existsByPrivilegeIdAndPrivilegeName(p.getPrivilegeId(), p.getPrivilegeName())) {
                privilegeRepository.save(p);
            }
        });

        Map<String, List<Privilege>> privMap = privileges.stream()
                .collect(Collectors.groupingBy(Privilege::getPrivilegeId));

        List<Role> roles = List.of(
                new Role("ADMIN", "시스템 관리자", "SAP 시스템 전체 관리", combinePrivileges(privMap, "S_USER_GRP", "S_TCODE", "S_USER_AUTH")),
                new Role("DEVELOPER", "개발자", "SAP 개발자 권한", combinePrivileges(privMap, "S_TCODE", "S_PROGRAM", "S_DEVELOP", "S_TRANSPRT")),
                new Role("SALES", "영업 담당자", "SAP 영업 기능 접근", combinePrivileges(privMap, "S_TCODE", "VA_VBAK_VBK", "SD_VBAK_AAT")),
                new Role("BASIS", "기술 관리자", "시스템 기술 지원", combinePrivileges(privMap, "S_RFC", "S_DATASET")),
                new Role("FILE_ADMIN", "파일 관리자", "파일 업로드/다운로드 권한", combinePrivileges(privMap, "S_DATASET")),
                new Role("HR", "인사 관리자", "SAP 인사 관리 기능", combinePrivileges(privMap, "P_ORGIN")),
                new Role("MM", "자재 관리자", "SAP 자재 관리 기능", combinePrivileges(privMap, "M_MATE_MAT", "MM_PUR_PO", "MM_PUR_PR")),
                new Role("FI", "재무 관리자", "SAP 재무 회계 기능", combinePrivileges(privMap, "F_BKPF_BUK", "FI_GL_ACC")),
                new Role("CO", "관리회계 관리자", "SAP 관리회계 기능", combinePrivileges(privMap, "K_KOSTL", "CO_CCTR")),
                new Role("PP", "생산 관리자", "SAP 생산 관리 기능", combinePrivileges(privMap, "PP_ORDER")),
                new Role("QM", "품질 관리자", "SAP 품질 관리 기능", combinePrivileges(privMap, "QM_QINFO")),
                new Role("PM", "설비 관리자", "SAP 설비 관리 기능", combinePrivileges(privMap, "PM_EQUI")),
                new Role("WM", "창고 관리자", "SAP 창고 관리 기능", combinePrivileges(privMap, "WM_LQUA")),
                new Role("SD", "판매 관리자", "SAP SD 모듈 기능", combinePrivileges(privMap, "SD_VBAK_AAT", "SD_BILLING")),
                new Role("BW", "BI 관리자", "SAP BW 분석 보고서 기능", combinePrivileges(privMap, "BW_REPORT")),
                new Role("APO", "계획 관리자", "SAP APO 계획 기능", combinePrivileges(privMap, "APO_PLAN")),
                new Role("IT_ADMIN", "IT 관리자", "SAP 시스템 IT 설정 및 모니터링", combinePrivileges(privMap, "IT_SEC", "IT_MON", "IT_CFG")),
                new Role("GRC", "접근 통제 관리자", "GRC 접근 권한 감사 및 분석", combinePrivileges(privMap, "GRC_ACCESS")),
                new Role("SOLMAN", "솔루션 매니저 관리자", "SAP 솔루션 매니저 기능", combinePrivileges(privMap, "SOLMAN_MON")),
                new Role("S4HANA", "S/4HANA 사용자", "SAP S/4HANA 핵심 기능", combinePrivileges(privMap, "S4HANA_CORE"))
                // 필요한 역할 계속 추가 가능
        );

        roleRepository.saveAll(roles);
        log.info("[initMasterData] 초기화 완료");
    }

    private List<Privilege> combinePrivileges(Map<String, List<Privilege>> map, String... keys) {
        return Arrays.stream(keys)
                .flatMap(k -> map.getOrDefault(k, Collections.emptyList()).stream())
                .collect(Collectors.toList());
    }
}