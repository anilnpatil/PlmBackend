package com.nextfirst.plm.sourcing.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextfirst.plm.sourcing.entity.ManufacturingComponentWorkflow;

public interface ManufacturingComponentWorkflowRepository
        extends JpaRepository<ManufacturingComponentWorkflow, Long> {
    Optional<ManufacturingComponentWorkflow> findByDesignPartId(Long designPartId);
    List<ManufacturingComponentWorkflow> findByDesignPartIdIn(Collection<Long> designPartIds);
}
