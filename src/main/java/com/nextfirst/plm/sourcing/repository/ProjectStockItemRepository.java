package com.nextfirst.plm.sourcing.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextfirst.plm.sourcing.entity.ProjectStockItem;

public interface ProjectStockItemRepository extends JpaRepository<ProjectStockItem, Long> {
    Optional<ProjectStockItem> findByDesignPartId(Long designPartId);
    List<ProjectStockItem> findByDesignPartIdIn(Collection<Long> designPartIds);
}
