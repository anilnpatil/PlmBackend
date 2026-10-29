package com.nextfirst.plm.design.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextfirst.plm.design.entity.DesignMainAssembly;

public interface DesignMainAssemblyRepository extends JpaRepository<DesignMainAssembly, Long> {
    List<DesignMainAssembly> findByProjectIdOrderById(Long projectId);
    Optional<DesignMainAssembly> findByIdAndProjectId(Long id, Long projectId);
    boolean existsByProjectIdAndAssemblyNumberIgnoreCase(Long projectId, String assemblyNumber);
}
