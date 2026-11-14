package com.nextfirst.plm.design.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nextfirst.plm.design.entity.DesignParts;
import com.nextfirst.plm.design.entity.ModeOfPurchase;
import com.nextfirst.plm.design.entity.PartStatus;

public interface DesignPartsRepository extends JpaRepository<DesignParts, Long> {
    Optional<DesignParts> findByIdAndMainAssemblyProjectId(Long id, Long projectId);

    @Query("""
            select part from DesignParts part
            where part.mainAssembly.project.id = :projectId
              and part.partStatus = :partStatus
              and (part.modeOfPurchase = :modeOfPurchase
                   or (:includeUnspecifiedMode = true and part.modeOfPurchase is null))
            order by part.partNumber
            """)
    List<DesignParts> findReadyForMode(
            @Param("projectId") Long projectId,
            @Param("modeOfPurchase") ModeOfPurchase modeOfPurchase,
            @Param("partStatus") PartStatus partStatus,
            @Param("includeUnspecifiedMode") boolean includeUnspecifiedMode);

    List<DesignParts> findByMainAssemblyIdOrderByPartNumber(Long mainAssemblyId);
    List<DesignParts> findBySubAssemblyIdOrderByPartNumber(Long subAssemblyId);
    boolean existsByMainAssemblyIdAndSubAssemblyIsNullAndPartNumberIgnoreCase(
            Long mainAssemblyId, String partNumber);
    boolean existsBySubAssemblyIdAndPartNumberIgnoreCase(Long subAssemblyId, String partNumber);
}
