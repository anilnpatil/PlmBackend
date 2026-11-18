package com.nextfirst.plm.sourcing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.nextfirst.plm.auth.service.AuthAuditService;
import com.nextfirst.plm.design.entity.DesignMainAssembly;
import com.nextfirst.plm.design.entity.DesignParts;
import com.nextfirst.plm.design.entity.ModeOfPurchase;
import com.nextfirst.plm.design.entity.PartStatus;
import com.nextfirst.plm.design.repository.DesignPartsRepository;
import com.nextfirst.plm.project.entity.Projects;
import com.nextfirst.plm.project.repository.ProjectRepository;
import com.nextfirst.plm.sourcing.entity.PurchaseComponentWorkflow;
import com.nextfirst.plm.sourcing.repository.ManufacturingComponentWorkflowRepository;
import com.nextfirst.plm.sourcing.repository.ProjectStockItemRepository;
import com.nextfirst.plm.sourcing.repository.PurchaseComponentWorkflowRepository;
import com.nextfirst.plm.supplier.entity.Supplier;
import com.nextfirst.plm.supplier.repository.SupplierRepository;

class ComponentQueueServiceTest {
    private final DesignPartsRepository designPartsRepository = mock(DesignPartsRepository.class);
    private final PurchaseComponentWorkflowRepository purchaseWorkflowRepository =
            mock(PurchaseComponentWorkflowRepository.class);
    private final ManufacturingComponentWorkflowRepository manufacturingWorkflowRepository =
            mock(ManufacturingComponentWorkflowRepository.class);
    private final ProjectStockItemRepository stockItemRepository = mock(ProjectStockItemRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final AuthAuditService auditService = mock(AuthAuditService.class);
    private final SupplierRepository supplierRepository = mock(SupplierRepository.class);
    private final SupplierSelectionResolver supplierSelectionResolver =
            new SupplierSelectionResolver(supplierRepository);

    @BeforeEach
    void setUp() {
        when(projectRepository.findById(42L)).thenReturn(Optional.of(new Projects()));
        when(designPartsRepository.findReadyForMode(
                42L, ModeOfPurchase.PURCHASE, PartStatus.READY_TO_PURCHASE, true)).thenReturn(List.of());
        when(designPartsRepository.findReadyForMode(
                42L, ModeOfPurchase.MANUFACTURE, PartStatus.READY_TO_PURCHASE, false)).thenReturn(List.of());
        when(designPartsRepository.findReadyForMode(
                42L, ModeOfPurchase.STOCK, PartStatus.READY_TO_PURCHASE, false)).thenReturn(List.of());
    }

    @Test
    void purchaseQueueUsesPurchaseModeAndReadyStatus() {
        PurchaseComponentsService service = new PurchaseComponentsService(
                designPartsRepository, purchaseWorkflowRepository, projectRepository, auditService,
                supplierSelectionResolver);

        assertTrue(service.components(42L, null).isEmpty());

        verify(designPartsRepository).findReadyForMode(
                42L, ModeOfPurchase.PURCHASE, PartStatus.READY_TO_PURCHASE, true);
    }

    @Test
    void manufacturingQueueUsesManufacturingModeAndReadyStatus() {
        ManufacturingComponentsService service = new ManufacturingComponentsService(
                designPartsRepository, manufacturingWorkflowRepository, projectRepository, auditService,
                supplierSelectionResolver);

        assertTrue(service.components(42L, null).isEmpty());

        verify(designPartsRepository).findReadyForMode(
                42L, ModeOfPurchase.MANUFACTURE, PartStatus.READY_TO_PURCHASE, false);
    }

    @Test
    void projectStockQueueUsesStockModeAndReadyStatus() {
        ProjectStockService service = new ProjectStockService(
                designPartsRepository, stockItemRepository, projectRepository, auditService);

        assertTrue(service.components(42L, null).isEmpty());

        verify(designPartsRepository).findReadyForMode(
                42L, ModeOfPurchase.STOCK, PartStatus.READY_TO_PURCHASE, false);
    }

    @Test
    void purchaseQueueIncludesReadyPartsWithUnspecifiedModeAndMapsPurchaseDetails() {
        DesignMainAssembly mainAssembly = new DesignMainAssembly();
        mainAssembly.setAssemblyNumber("MAIN-42");
        DesignParts part = new DesignParts();
        ReflectionTestUtils.setField(part, "id", 101L);
        part.setMainAssembly(mainAssembly);
        part.setPartNumber("P-101");
        part.setPartName("Bearing");
        part.setQuantity(new BigDecimal("2"));
        part.setPartStatus(PartStatus.READY_TO_PURCHASE);
        PurchaseComponentWorkflow workflow = new PurchaseComponentWorkflow();
        workflow.setDesignPart(part);
        Supplier supplier = new Supplier();
        supplier.setName("Acme Supplies");
        workflow.setSupplier(supplier);
        when(designPartsRepository.findReadyForMode(
                42L, ModeOfPurchase.PURCHASE, PartStatus.READY_TO_PURCHASE, true))
                .thenReturn(List.of(part));
        when(purchaseWorkflowRepository.findByDesignPartIdIn(List.of(101L))).thenReturn(List.of(workflow));

        PurchaseComponentsService service = new PurchaseComponentsService(
                designPartsRepository, purchaseWorkflowRepository, projectRepository, auditService,
                supplierSelectionResolver);

        var components = service.components(42L, null);

        assertEquals(1, components.size());
        assertEquals(101L, components.get(0).designPartId());
        assertEquals("P-101", components.get(0).partNumber());
        assertEquals("Bearing", components.get(0).partName());
        assertEquals("Acme Supplies", components.get(0).supplierName());
        assertEquals(new BigDecimal("2"), components.get(0).quantity());
        assertEquals("MAIN-42", components.get(0).mainAssemblyNumber());
    }

    @Test
    void manufacturingQueueReturnsReadyManufacturedParts() {
        DesignMainAssembly mainAssembly = new DesignMainAssembly();
        mainAssembly.setAssemblyNumber("MAIN-42");
        DesignParts part = new DesignParts();
        ReflectionTestUtils.setField(part, "id", 102L);
        part.setMainAssembly(mainAssembly);
        part.setPartNumber("M-102");
        part.setPartName("Housing");
        part.setQuantity(new BigDecimal("1"));
        part.setModeOfPurchase(ModeOfPurchase.MANUFACTURE);
        part.setPartStatus(PartStatus.READY_TO_PURCHASE);
        when(designPartsRepository.findReadyForMode(
                42L, ModeOfPurchase.MANUFACTURE, PartStatus.READY_TO_PURCHASE, false))
                .thenReturn(List.of(part));
        when(manufacturingWorkflowRepository.findByDesignPartIdIn(List.of(102L))).thenReturn(List.of());

        ManufacturingComponentsService service = new ManufacturingComponentsService(
                designPartsRepository, manufacturingWorkflowRepository, projectRepository, auditService,
                supplierSelectionResolver);

        var components = service.components(42L, null);

        assertEquals(1, components.size());
        assertEquals(102L, components.get(0).designPartId());
        assertEquals("M-102", components.get(0).partNumber());
        assertEquals("Housing", components.get(0).partName());
        assertEquals(new BigDecimal("1"), components.get(0).quantity());
    }
}
