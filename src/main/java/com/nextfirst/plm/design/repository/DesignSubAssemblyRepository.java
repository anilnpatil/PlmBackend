package com.nextfirst.plm.design.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextfirst.plm.design.entity.DesignSubAssembly;

public interface DesignSubAssemblyRepository extends JpaRepository<DesignSubAssembly, Long> {
    List<DesignSubAssembly> findByMainAssemblyIdOrderById(Long mainAssemblyId);
    Optional<DesignSubAssembly> findByIdAndMainAssemblyProjectId(Long id, Long projectId);
    boolean existsByMainAssemblyIdAndAssemblyNumberIgnoreCase(Long mainAssemblyId, String assemblyNumber);
}
