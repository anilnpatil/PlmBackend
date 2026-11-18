package com.nextfirst.plm.sourcing.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.nextfirst.plm.sourcing.entity.PurchaseComponentWorkflow;

public interface PurchaseComponentWorkflowRepository
        extends JpaRepository<PurchaseComponentWorkflow, Long> {
    Optional<PurchaseComponentWorkflow> findByDesignPartId(Long designPartId);
    @EntityGraph(attributePaths = "supplier")
    List<PurchaseComponentWorkflow> findByDesignPartIdIn(Collection<Long> designPartIds);
}
