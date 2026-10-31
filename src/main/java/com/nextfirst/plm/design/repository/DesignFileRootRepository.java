package com.nextfirst.plm.design.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextfirst.plm.design.entity.DesignFileRoot;

public interface DesignFileRootRepository extends JpaRepository<DesignFileRoot, Long> {
    List<DesignFileRoot> findByProjectIdOrderById(Long projectId);
    Optional<DesignFileRoot> findByIdAndProjectId(Long id, Long projectId);
    boolean existsByProjectIdAndPathIgnoreCase(Long projectId, String path);
}
