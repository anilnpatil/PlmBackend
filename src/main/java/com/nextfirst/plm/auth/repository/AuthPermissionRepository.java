package com.nextfirst.plm.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextfirst.plm.auth.entity.AuthPermission;

public interface AuthPermissionRepository extends JpaRepository<AuthPermission, Long> {
    Optional<AuthPermission> findByCodeIgnoreCase(String code);
}
