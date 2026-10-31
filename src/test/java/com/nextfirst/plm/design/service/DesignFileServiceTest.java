package com.nextfirst.plm.design.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.nextfirst.plm.auth.service.AuthAuditService;
import com.nextfirst.plm.auth.service.AuthorizationService;
import com.nextfirst.plm.design.entity.DesignFileRoot;
import com.nextfirst.plm.design.entity.DesignParts;
import com.nextfirst.plm.design.exception.DesignResourceNotFoundException;
import com.nextfirst.plm.design.repository.DesignFileRootRepository;
import com.nextfirst.plm.design.repository.DesignPartsRepository;
import com.nextfirst.plm.project.entity.Projects;
import com.nextfirst.plm.project.repository.ProjectRepository;

class DesignFileServiceTest {
    private final DesignPartsRepository partsRepository = mock(DesignPartsRepository.class);
    private final DesignFileRootRepository fileRootRepository = mock(DesignFileRootRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final AuthAuditService auditService = mock(AuthAuditService.class);
    private final AuthorizationService authorizationService = mock(AuthorizationService.class);

    @Test
    void resolvesFileInsideConfiguredRoot(@TempDir Path tempDir) throws IOException {
        Path file = Files.writeString(tempDir.resolve("part.pdf"), "design");
        DesignParts part = new DesignParts();
        part.setFilePath(file.toString());
        when(partsRepository.findByIdAndMainAssemblyProjectId(5L, 2L)).thenReturn(Optional.of(part));
        when(fileRootRepository.findByProjectIdOrderById(2L)).thenReturn(java.util.List.of(fileRoot(tempDir)));

        DesignFileService service = service(tempDir);

        assertEquals(file.toRealPath(), service.getPartFile(2L, 5L));
    }

    @Test
    void rejectsFileOutsideConfiguredRoot(@TempDir Path tempDir) throws IOException {
        Path allowedRoot = Files.createDirectory(tempDir.resolve("allowed"));
        Path outsideFile = Files.writeString(tempDir.resolve("outside.pdf"), "private");
        DesignParts part = new DesignParts();
        part.setFilePath(outsideFile.toString());
        when(partsRepository.findByIdAndMainAssemblyProjectId(5L, 2L)).thenReturn(Optional.of(part));
        when(fileRootRepository.findByProjectIdOrderById(2L)).thenReturn(java.util.List.of(fileRoot(allowedRoot)));

        DesignFileService service = service(allowedRoot);

        assertThrows(DesignResourceNotFoundException.class, () -> service.getPartFile(2L, 5L));
    }

    @Test
    void browsesServerFilesAndReturnsFullPath(@TempDir Path tempDir) throws IOException {
        Path folder = Files.createDirectory(tempDir.resolve("subassembly"));
        Path file = Files.writeString(folder.resolve("10104 stand lock.pdf"), "design");
        DesignFileRoot root = fileRoot(tempDir);
        when(fileRootRepository.findByProjectIdOrderById(2L)).thenReturn(java.util.List.of(root));
        DesignFileService service = service(tempDir);

        var response = service.browse(2L, 12L, "subassembly");

        assertEquals("subassembly", response.currentDirectory());
        assertEquals(1, response.entries().size());
        assertEquals(file.toRealPath().toString(), response.entries().get(0).fullPath());
        assertTrue(response.entries().get(0).name().endsWith(".pdf"));
    }

    @Test
    void rejectsBrowsePathOutsideConfiguredRoot(@TempDir Path tempDir) throws IOException {
        Path allowedRoot = Files.createDirectory(tempDir.resolve("allowed"));
        DesignFileRoot root = fileRoot(allowedRoot);
        when(fileRootRepository.findByProjectIdOrderById(2L)).thenReturn(java.util.List.of(root));
        DesignFileService service = service(allowedRoot);

        assertThrows(IllegalArgumentException.class, () -> service.browse(2L, 12L, ".."));
    }

    @Test
    void rejectsPartFileWithoutRegisteredProjectRoot(@TempDir Path tempDir) throws IOException {
        Path file = Files.writeString(tempDir.resolve("part.pdf"), "design");
        DesignParts part = new DesignParts();
        part.setFilePath(file.toString());
        when(partsRepository.findByIdAndMainAssemblyProjectId(5L, 2L)).thenReturn(Optional.of(part));
        DesignFileService service = service(Path.of(""));

        assertThrows(DesignResourceNotFoundException.class, () -> service.getPartFile(2L, 5L));
    }

    @Test
    void addsProjectFolderOnlyWithinConfiguredBase(@TempDir Path tempDir) throws IOException {
        Path projectFolder = Files.createDirectory(tempDir.resolve("project"));
        Projects project = new Projects();
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(fileRootRepository.save(org.mockito.ArgumentMatchers.any(DesignFileRoot.class)))
                .thenAnswer(invocation -> {
                    DesignFileRoot root = invocation.getArgument(0);
                    ReflectionTestUtils.setField(root, "id", 18L);
                    return root;
                });
        DesignFileService service = service(tempDir);

        var result = service.addRoot(
                2L,
                new com.nextfirst.plm.design.dto.DesignFileRootRequest(projectFolder.toString()),
                UsernamePasswordAuthenticationToken.authenticated("designer@example.com", "token", java.util.List.of()));

        assertEquals(projectFolder.toRealPath().toString(), result.path());
    }

    @Test
    void rejectsProjectFolderOutsideConfiguredBase(@TempDir Path tempDir) throws IOException {
        Path allowedBase = Files.createDirectory(tempDir.resolve("allowed"));
        Path outsideFolder = Files.createDirectory(tempDir.resolve("private"));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(new Projects()));
        DesignFileService service = service(allowedBase);

        assertThrows(IllegalArgumentException.class, () -> service.addRoot(
                2L,
                new com.nextfirst.plm.design.dto.DesignFileRootRequest(outsideFolder.toString()),
                UsernamePasswordAuthenticationToken.authenticated("designer@example.com", "token", java.util.List.of())));
    }

    private DesignFileService service(Path allowedRoot) {
        return new DesignFileService(
                partsRepository,
                fileRootRepository,
                projectRepository,
                auditService,
                authorizationService,
                allowedRoot.toString());
    }

    private DesignFileRoot fileRoot(Path path) {
        DesignFileRoot root = new DesignFileRoot();
        root.setPath(path.toString());
        ReflectionTestUtils.setField(root, "id", 12L);
        return root;
    }
}
