package com.dreamsecurity.sapmock.repository;

import com.dreamsecurity.sapmock.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
}
