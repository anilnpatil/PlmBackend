package com.nextfirst.plm.auth.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nextfirst.plm.auth.entity.AuthAuditEvent;

public interface AuthAuditEventRepository extends JpaRepository<AuthAuditEvent, Long> {
    Page<AuthAuditEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
