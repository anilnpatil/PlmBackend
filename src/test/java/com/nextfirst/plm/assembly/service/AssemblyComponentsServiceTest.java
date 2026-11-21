package com.nextfirst.plm.assembly.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.nextfirst.plm.assembly.dto.AssemblyComponentResponse;
import com.nextfirst.plm.assembly.entity.AssemblyComponentWorkflow;
import com.nextfirst.plm.assembly.entity.AssemblyStatus;
import com.nextfirst.plm.assembly.repository.AssemblyComponentWorkflowRepository;
import com.nextfirst.plm.auth.service.AuthAuditService;
import com.nextfirst.plm.design.entity.DesignMainAssembly;
import com.nextfirst.plm.design.entity.DesignParts;
import com.nextfirst.plm.design.entity.ModeOfPurchase;
import com.nextfirst.plm.design.entity.PartStatus;
import com.nextfirst.plm.project.entity.Projects;
import com.nextfirst.plm.project.repository.ProjectRepository;
import com.nextfirst.plm.sourcing.entity.AvailabilityStatus;
import com.nextfirst.plm.sourcing.entity.PartReceivedStatus;

class AssemblyComponentsServiceTest {
    private final AssemblyComponentWorkflowRepository workflowRepository =
            org.mockito.Mockito.mock(AssemblyComponentWorkflowRepository.class);
    private final ProjectRepository projectRepository = org.mockito.Mockito.mock(ProjectRepository.class);
    private final AuthAuditService auditService = org.mockito.Mockito.mock(AuthAuditService.class);
    private final AssemblyComponentsService service =
            new AssemblyComponentsService(workflowRepository, projectRepository, auditService);

    @BeforeEach
    void setUp() {
        when(projectRepository.findById(7L)).thenReturn(Optional.of(new Projects()));
    }

    @Test
    void componentsUsesReceivedAndAvailableSourcesAndReturnsGroupedPartData() {
        AssemblyComponentResponse component = new AssemblyComponentResponse(
                31L, 3L, "MAIN-3", "Main assembly", 4L, "SUB-4", "Sub assembly",
                "P-31", "Bracket", new BigDecimal("2"), ModeOfPurchase.PURCHASE,
                null, null, null);
        when(workflowRepository.findEligibleComponents(
                7L,
                ModeOfPurchase.PURCHASE,
                ModeOfPurchase.MANUFACTURE,
                ModeOfPurchase.STOCK,
                PartReceivedStatus.RECEIVED,
                PartStatus.READY_TO_PURCHASE,
                AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(component));

        List<AssemblyComponentResponse> result = service.components(7L, null);

        assertEquals(1, result.size());
        assertEquals("MAIN-3", result.get(0).mainAssemblyNumber());
        assertEquals("SUB-4", result.get(0).subAssemblyNumber());
        assertEquals(AssemblyStatus.NOT_STARTED, result.get(0).assemblyStatus());
        verify(workflowRepository).findEligibleComponents(
                7L,
                ModeOfPurchase.PURCHASE,
                ModeOfPurchase.MANUFACTURE,
                ModeOfPurchase.STOCK,
                PartReceivedStatus.RECEIVED,
                PartStatus.READY_TO_PURCHASE,
                AvailabilityStatus.AVAILABLE);
    }

    @Test
    void startStampsCurrentDateAndSetsInProgress() {
        DesignParts part = eligiblePart();
        when(workflowRepository.findEligiblePart(
                7L, 31L, ModeOfPurchase.PURCHASE, ModeOfPurchase.MANUFACTURE, ModeOfPurchase.STOCK,
                PartReceivedStatus.RECEIVED, PartStatus.READY_TO_PURCHASE, AvailabilityStatus.AVAILABLE))
                .thenReturn(Optional.of(part));
        when(workflowRepository.findByDesignPartId(31L)).thenReturn(Optional.empty());
        when(workflowRepository.save(any(AssemblyComponentWorkflow.class)))
                .thenAnswer(invocation -> {
                    AssemblyComponentWorkflow workflow = invocation.getArgument(0);
                    ReflectionTestUtils.setField(workflow, "id", 81L);
                    return workflow;
                });

        AssemblyComponentResponse result = service.start(7L, 31L, null);

        assertEquals(AssemblyStatus.IN_PROGRESS, result.assemblyStatus());
        assertEquals(LocalDate.now(), result.assemblyStartDate());
        assertNull(result.assemblyCompleteDate());
        verify(auditService).record(
                "ASSEMBLY_WORKFLOW_STARTED", "ASSEMBLY_COMPONENT_WORKFLOW", 81L, "P-31");
    }

    @Test
    void completeStampsCurrentDateAndSetsCompleted() {
        DesignParts part = eligiblePart();
        AssemblyComponentWorkflow workflow = new AssemblyComponentWorkflow();
        ReflectionTestUtils.setField(workflow, "id", 81L);
        workflow.setAssemblyStartDate(LocalDate.now().minusDays(1));
        workflow.setAssemblyStatus(AssemblyStatus.IN_PROGRESS);
        when(workflowRepository.findEligiblePart(
                7L, 31L, ModeOfPurchase.PURCHASE, ModeOfPurchase.MANUFACTURE, ModeOfPurchase.STOCK,
                PartReceivedStatus.RECEIVED, PartStatus.READY_TO_PURCHASE, AvailabilityStatus.AVAILABLE))
                .thenReturn(Optional.of(part));
        when(workflowRepository.findByDesignPartId(31L)).thenReturn(Optional.of(workflow));
        when(workflowRepository.save(any(AssemblyComponentWorkflow.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssemblyComponentResponse result = service.complete(7L, 31L, null);

        assertEquals(AssemblyStatus.COMPLETED, result.assemblyStatus());
        assertEquals(LocalDate.now(), result.assemblyCompleteDate());
        verify(auditService).record(
                "ASSEMBLY_WORKFLOW_COMPLETED", "ASSEMBLY_COMPONENT_WORKFLOW", 81L, "P-31");
    }

    @Test
    void startRejectsPartsThatHaveAlreadyStarted() {
        DesignParts part = eligiblePart();
        AssemblyComponentWorkflow workflow = new AssemblyComponentWorkflow();
        workflow.setAssemblyStartDate(LocalDate.now().minusDays(1));
        workflow.setAssemblyStatus(AssemblyStatus.IN_PROGRESS);
        when(workflowRepository.findEligiblePart(
                7L, 31L, ModeOfPurchase.PURCHASE, ModeOfPurchase.MANUFACTURE, ModeOfPurchase.STOCK,
                PartReceivedStatus.RECEIVED, PartStatus.READY_TO_PURCHASE, AvailabilityStatus.AVAILABLE))
                .thenReturn(Optional.of(part));
        when(workflowRepository.findByDesignPartId(31L)).thenReturn(Optional.of(workflow));

        assertThrows(IllegalArgumentException.class, () -> service.start(7L, 31L, null));
        verify(workflowRepository, never()).save(any(AssemblyComponentWorkflow.class));
        verifyNoInteractions(auditService);
    }

    @Test
    void completeRejectsPartsThatHaveNotStarted() {
        DesignParts part = eligiblePart();
        when(workflowRepository.findEligiblePart(
                7L, 31L, ModeOfPurchase.PURCHASE, ModeOfPurchase.MANUFACTURE, ModeOfPurchase.STOCK,
                PartReceivedStatus.RECEIVED, PartStatus.READY_TO_PURCHASE, AvailabilityStatus.AVAILABLE))
                .thenReturn(Optional.of(part));
        when(workflowRepository.findByDesignPartId(31L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.complete(7L, 31L, null));
        verify(workflowRepository, never()).save(any(AssemblyComponentWorkflow.class));
        verifyNoInteractions(auditService);
    }

    private DesignParts eligiblePart() {
        DesignMainAssembly mainAssembly = new DesignMainAssembly();
        ReflectionTestUtils.setField(mainAssembly, "id", 3L);
        mainAssembly.setAssemblyNumber("MAIN-3");
        mainAssembly.setName("Main assembly");
        DesignParts part = new DesignParts();
        ReflectionTestUtils.setField(part, "id", 31L);
        part.setMainAssembly(mainAssembly);
        part.setPartNumber("P-31");
        part.setPartName("Bracket");
        part.setQuantity(new BigDecimal("2"));
        part.setModeOfPurchase(ModeOfPurchase.PURCHASE);
        return part;
    }

}
