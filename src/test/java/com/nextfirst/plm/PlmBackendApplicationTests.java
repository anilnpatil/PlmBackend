package com.nextfirst.plm;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nextfirst.plm.assembly.repository.AssemblyComponentWorkflowRepository;
import com.nextfirst.plm.design.entity.ModeOfPurchase;
import com.nextfirst.plm.design.entity.PartStatus;
import com.nextfirst.plm.sourcing.entity.AvailabilityStatus;
import com.nextfirst.plm.sourcing.entity.PartReceivedStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PlmBackendApplicationTests {
    @Autowired
    private AssemblyComponentWorkflowRepository assemblyWorkflowRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void assemblyEligibilityQueriesRunForProjectsWithoutComponents() {
        assertTrue(assemblyWorkflowRepository.findEligibleComponents(
                -1L,
                ModeOfPurchase.PURCHASE,
                ModeOfPurchase.MANUFACTURE,
                ModeOfPurchase.STOCK,
                PartReceivedStatus.RECEIVED,
                PartStatus.READY_TO_PURCHASE,
                AvailabilityStatus.AVAILABLE).isEmpty());
        assertTrue(assemblyWorkflowRepository.findEligiblePart(
                -1L,
                -1L,
                ModeOfPurchase.PURCHASE,
                ModeOfPurchase.MANUFACTURE,
                ModeOfPurchase.STOCK,
                PartReceivedStatus.RECEIVED,
                PartStatus.READY_TO_PURCHASE,
                AvailabilityStatus.AVAILABLE).isEmpty());
    }

}
