package com.nextfirst.plm.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextfirst.plm.auth.entity.AuthRole;

public interface AuthRoleRepository extends JpaRepository<AuthRole, Long> {
    Optional<AuthRole> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
