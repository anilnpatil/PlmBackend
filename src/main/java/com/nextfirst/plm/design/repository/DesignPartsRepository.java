package com.nextfirst.plm.design.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextfirst.plm.design.entity.DesignParts;

public interface DesignPartsRepository extends JpaRepository<DesignParts, Long> {
    Optional<DesignParts> findByIdAndMainAssemblyProjectId(Long id, Long projectId);
    List<DesignParts> findByMainAssemblyIdOrderByPartNumber(Long mainAssemblyId);
    List<DesignParts> findBySubAssemblyIdOrderByPartNumber(Long subAssemblyId);
    boolean existsByMainAssemblyIdAndSubAssemblyIsNullAndPartNumberIgnoreCase(
            Long mainAssemblyId, String partNumber);
    boolean existsBySubAssemblyIdAndPartNumberIgnoreCase(Long subAssemblyId, String partNumber);
}
