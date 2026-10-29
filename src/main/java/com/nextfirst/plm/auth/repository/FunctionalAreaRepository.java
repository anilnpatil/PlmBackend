package com.nextfirst.plm.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextfirst.plm.auth.entity.FunctionalArea;

public interface FunctionalAreaRepository extends JpaRepository<FunctionalArea, Long> {
    Optional<FunctionalArea> findByCodeIgnoreCase(String code);
}
