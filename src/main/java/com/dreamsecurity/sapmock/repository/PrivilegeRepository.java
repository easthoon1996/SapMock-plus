package com.dreamsecurity.sapmock.repository;

import com.dreamsecurity.sapmock.model.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    boolean existsByPrivilegeIdAndPrivilegeName(String privilegeId, String privilegeName);
    List<Privilege> findByPrivilegeId(String privilegeId);
}
