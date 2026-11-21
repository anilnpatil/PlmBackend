package com.nextfirst.plm.assembly.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nextfirst.plm.assembly.dto.AssemblyComponentResponse;
import com.nextfirst.plm.assembly.entity.AssemblyComponentWorkflow;
import com.nextfirst.plm.assembly.entity.AssemblyStatus;
import com.nextfirst.plm.design.entity.DesignParts;
import com.nextfirst.plm.design.entity.ModeOfPurchase;
import com.nextfirst.plm.design.entity.PartStatus;
import com.nextfirst.plm.sourcing.entity.AvailabilityStatus;
import com.nextfirst.plm.sourcing.entity.PartReceivedStatus;

public interface AssemblyComponentWorkflowRepository
        extends JpaRepository<AssemblyComponentWorkflow, Long> {
    Optional<AssemblyComponentWorkflow> findByDesignPartId(Long designPartId);

    @Query("""
            select new com.nextfirst.plm.assembly.dto.AssemblyComponentResponse(
                part.id,
                main.id,
                main.assemblyNumber,
                main.name,
                sub.id,
                sub.assemblyNumber,
                sub.name,
                part.partNumber,
                part.partName,
                part.quantity,
                part.modeOfPurchase,
                workflow.assemblyStartDate,
                workflow.assemblyCompleteDate,
                workflow.assemblyStatus)
            from DesignParts part
            join part.mainAssembly main
            left join part.subAssembly sub
            left join AssemblyComponentWorkflow workflow on workflow.designPart = part
            where main.project.id = :projectId
              and part.partStatus = :readyStatus
              and (
                ((part.modeOfPurchase = :purchaseMode or part.modeOfPurchase is null)
                    and exists (
                        select purchaseWorkflow.id
                        from PurchaseComponentWorkflow purchaseWorkflow
                        where purchaseWorkflow.designPart = part
                          and purchaseWorkflow.partReceivedStatus = :receivedStatus))
                or (part.modeOfPurchase = :manufacturingMode
                    and exists (
                        select manufacturingWorkflow.id
                        from ManufacturingComponentWorkflow manufacturingWorkflow
                        where manufacturingWorkflow.designPart = part
                          and manufacturingWorkflow.partReceivedStatus = :receivedStatus))
                or (part.modeOfPurchase = :stockMode
                    and (
                        not exists (
                            select stockItem.id
                            from ProjectStockItem stockItem
                            where stockItem.designPart = part)
                        or exists (
                            select availableStockItem.id
                            from ProjectStockItem availableStockItem
                            where availableStockItem.designPart = part
                              and availableStockItem.availabilityStatus = :availabilityStatus)))
              )
            order by main.assemblyNumber, sub.assemblyNumber, part.partNumber
            """)
    List<AssemblyComponentResponse> findEligibleComponents(
            @Param("projectId") Long projectId,
            @Param("purchaseMode") ModeOfPurchase purchaseMode,
            @Param("manufacturingMode") ModeOfPurchase manufacturingMode,
            @Param("stockMode") ModeOfPurchase stockMode,
            @Param("receivedStatus") PartReceivedStatus receivedStatus,
            @Param("readyStatus") PartStatus readyStatus,
            @Param("availabilityStatus") AvailabilityStatus availabilityStatus);

    @Query("""
            select part
            from DesignParts part
            where part.id = :designPartId
              and part.mainAssembly.project.id = :projectId
              and part.partStatus = :readyStatus
              and (
                ((part.modeOfPurchase = :purchaseMode or part.modeOfPurchase is null)
                    and exists (
                        select purchaseWorkflow.id
                        from PurchaseComponentWorkflow purchaseWorkflow
                        where purchaseWorkflow.designPart = part
                          and purchaseWorkflow.partReceivedStatus = :receivedStatus))
                or (part.modeOfPurchase = :manufacturingMode
                    and exists (
                        select manufacturingWorkflow.id
                        from ManufacturingComponentWorkflow manufacturingWorkflow
                        where manufacturingWorkflow.designPart = part
                          and manufacturingWorkflow.partReceivedStatus = :receivedStatus))
                or (part.modeOfPurchase = :stockMode
                    and (
                        not exists (
                            select stockItem.id
                            from ProjectStockItem stockItem
                            where stockItem.designPart = part)
                        or exists (
                            select availableStockItem.id
                            from ProjectStockItem availableStockItem
                            where availableStockItem.designPart = part
                              and availableStockItem.availabilityStatus = :availabilityStatus)))
              )
            """)
    Optional<DesignParts> findEligiblePart(
            @Param("projectId") Long projectId,
            @Param("designPartId") Long designPartId,
            @Param("purchaseMode") ModeOfPurchase purchaseMode,
            @Param("manufacturingMode") ModeOfPurchase manufacturingMode,
            @Param("stockMode") ModeOfPurchase stockMode,
            @Param("receivedStatus") PartReceivedStatus receivedStatus,
            @Param("readyStatus") PartStatus readyStatus,
            @Param("availabilityStatus") AvailabilityStatus availabilityStatus);
}
