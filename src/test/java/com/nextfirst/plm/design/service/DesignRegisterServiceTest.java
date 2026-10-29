package com.nextfirst.plm.design.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.nextfirst.plm.auth.service.AuthAuditService;
import com.nextfirst.plm.auth.service.AuthorizationService;
import com.nextfirst.plm.design.dto.DesignPartsRequest;
import com.nextfirst.plm.design.entity.DesignMainAssembly;
import com.nextfirst.plm.design.entity.DesignParts;
import com.nextfirst.plm.design.entity.DesignSubAssembly;
import com.nextfirst.plm.design.repository.DesignMainAssemblyRepository;
import com.nextfirst.plm.design.repository.DesignPartsRepository;
import com.nextfirst.plm.design.repository.DesignSubAssemblyRepository;
import com.nextfirst.plm.project.entity.Projects;
import com.nextfirst.plm.project.repository.ProjectRepository;

class DesignRegisterServiceTest {
    private final DesignMainAssemblyRepository mainAssemblyRepository = mock(DesignMainAssemblyRepository.class);
    private final DesignSubAssemblyRepository subAssemblyRepository = mock(DesignSubAssemblyRepository.class);
    private final DesignPartsRepository partsRepository = mock(DesignPartsRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final AuthorizationService authorizationService = mock(AuthorizationService.class);
    private final AuthAuditService auditService = mock(AuthAuditService.class);

    private final DesignRegisterService service = new DesignRegisterService(
            mainAssemblyRepository,
            subAssemblyRepository,
            partsRepository,
            projectRepository,
            authorizationService,
            auditService);

    private Projects project;
    private DesignMainAssembly mainAssembly;
    private DesignSubAssembly subAssembly;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        project = new Projects();
        ReflectionTestUtils.setField(project, "id", 1L);
        mainAssembly = new DesignMainAssembly();
        mainAssembly.setProject(project);
        ReflectionTestUtils.setField(mainAssembly, "id", 10L);
        subAssembly = new DesignSubAssembly();
        subAssembly.setMainAssembly(mainAssembly);
        ReflectionTestUtils.setField(subAssembly, "id", 20L);
        authentication = UsernamePasswordAuthenticationToken.authenticated(
                "designer@example.com", "token", java.util.List.of());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(mainAssemblyRepository.findByIdAndProjectId(10L, 1L)).thenReturn(Optional.of(mainAssembly));
        when(subAssemblyRepository.findByIdAndMainAssemblyProjectId(20L, 1L))
                .thenReturn(Optional.of(subAssembly));
        when(partsRepository.save(any(DesignParts.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void partCanBeAssignedToSubAssemblyAndStoresOnlyItsPath() {
        var response = service.createPart(1L, new DesignPartsRequest(
                10L, 20L, "P-100", "Bracket", new BigDecimal("2"),
                "Mounting bracket", "in-house", "C:\\designs\\P-100.pdf",
                LocalDate.of(2026, 10, 21), null), authentication);

        assertEquals(10L, response.mainAssemblyId());
        assertEquals(20L, response.subAssemblyId());
        assertEquals("C:\\designs\\P-100.pdf", response.filePath());
        verify(partsRepository).save(any(DesignParts.class));
    }

    @Test
    void partCanBeAssignedDirectlyToMainAssembly() {
        var response = service.createPart(1L, new DesignPartsRequest(
                10L, null, "P-101", "Cover", BigDecimal.ONE,
                null, null, "C:\\designs\\P-101.pdf", null, null), authentication);

        assertEquals(10L, response.mainAssemblyId());
        assertNull(response.subAssemblyId());
    }

    @Test
    void rejectsCompletedDateEarlierThanStartDate() {
        assertThrows(IllegalArgumentException.class, () -> service.createPart(1L, new DesignPartsRequest(
                10L, null, "P-102", "Panel", BigDecimal.ONE,
                null, null, null, LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 21)),
                authentication));

        verify(partsRepository, never()).save(any(DesignParts.class));
    }

    @Test
    void rejectsSubAssemblyFromAnotherMainAssembly() {
        DesignMainAssembly otherMainAssembly = new DesignMainAssembly();
        ReflectionTestUtils.setField(otherMainAssembly, "id", 11L);
        subAssembly.setMainAssembly(otherMainAssembly);

        assertThrows(IllegalArgumentException.class, () -> service.createPart(1L, new DesignPartsRequest(
                10L, 20L, "P-103", "Panel", BigDecimal.ONE,
                null, null, null, null, null), authentication));

        verify(partsRepository, never()).save(any(DesignParts.class));
    }

    @Test
    void workspaceListsSubAssemblyPartsOnlyUnderTheirSubAssembly() {
        DesignParts part = new DesignParts();
        part.setMainAssembly(mainAssembly);
        part.setSubAssembly(subAssembly);
        part.setPartNumber("P-100");
        part.setPartName("Bracket");
        part.setQuantity(BigDecimal.ONE);

        when(mainAssemblyRepository.findByProjectIdOrderById(1L)).thenReturn(List.of(mainAssembly));
        when(subAssemblyRepository.findByMainAssemblyIdOrderById(10L)).thenReturn(List.of(subAssembly));
        when(partsRepository.findByMainAssemblyIdOrderByPartNumber(10L)).thenReturn(List.of(part));
        when(partsRepository.findBySubAssemblyIdOrderByPartNumber(20L)).thenReturn(List.of(part));
        when(authorizationService.hasProjectPermission(authentication, 1L, "PARTS_READ", "DESIGN"))
                .thenReturn(true);

        var workspace = service.workspace(1L, authentication);

        assertEquals(0, workspace.mainAssemblies().get(0).parts().size());
        assertEquals(1, workspace.mainAssemblies().get(0).subAssemblies().get(0).parts().size());
    }
}
