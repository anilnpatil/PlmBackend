package com.nextfirst.plm.design.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextfirst.plm.auth.service.AuthAuditService;
import com.nextfirst.plm.auth.service.AuthorizationService;
import com.nextfirst.plm.design.dto.DesignFileBrowserEntry;
import com.nextfirst.plm.design.dto.DesignFileBrowserResponse;
import com.nextfirst.plm.design.dto.DesignFileBrowserRoot;
import com.nextfirst.plm.design.dto.DesignFileRootRequest;
import com.nextfirst.plm.design.dto.DesignFileRootResponse;
import com.nextfirst.plm.design.dto.DesignFileRootsResponse;
import com.nextfirst.plm.design.entity.DesignFileRoot;
import com.nextfirst.plm.design.entity.DesignParts;
import com.nextfirst.plm.design.exception.DesignResourceNotFoundException;
import com.nextfirst.plm.design.repository.DesignFileRootRepository;
import com.nextfirst.plm.design.repository.DesignPartsRepository;
import com.nextfirst.plm.project.entity.Projects;
import com.nextfirst.plm.project.exception.ProjectNotFoundException;
import com.nextfirst.plm.project.repository.ProjectRepository;

@Service
public class DesignFileService {
    private final DesignPartsRepository partsRepository;
    private final DesignFileRootRepository fileRootRepository;
    private final ProjectRepository projectRepository;
    private final AuthAuditService auditService;
    private final AuthorizationService authorizationService;
    private final String allowedRootsConfiguration;

    public DesignFileService(
            DesignPartsRepository partsRepository,
            DesignFileRootRepository fileRootRepository,
            ProjectRepository projectRepository,
            AuthAuditService auditService,
            AuthorizationService authorizationService,
            @Value("${plm.design-files.allowed-roots:}") String allowedRootsConfiguration) {
        this.partsRepository = partsRepository;
        this.fileRootRepository = fileRootRepository;
        this.projectRepository = projectRepository;
        this.auditService = auditService;
        this.authorizationService = authorizationService;
        this.allowedRootsConfiguration = allowedRootsConfiguration;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PARTS_READ', 'DESIGN') or "
            + "@authorizationService.hasProjectPermission(authentication, #p0, 'PARTS_UPDATE', 'DESIGN')")
    public Path getPartFile(
            @P("projectId") Long projectId,
            Long partId) {
        DesignParts part = partsRepository.findByIdAndMainAssemblyProjectId(partId, projectId)
                .orElseThrow(() -> new DesignResourceNotFoundException("Part", partId));
        if (part.getFilePath() == null || part.getFilePath().isBlank()) {
            throw new DesignResourceNotFoundException("Part file", partId);
        }

        try {
            Path file = Path.of(part.getFilePath().trim()).toRealPath();
            if (!Files.isRegularFile(file)) {
                throw new DesignResourceNotFoundException("Part file", partId);
            }
            boolean allowed = fileRootRepository.findByProjectIdOrderById(projectId).stream()
                    .map(root -> resolveRegisteredRoot(root.getPath()))
                    .anyMatch(root -> file.startsWith(root));
            if (!allowed) {
                throw new DesignResourceNotFoundException("Part file", partId);
            }
            return file;
        } catch (IOException | InvalidPathException exception) {
            throw new DesignResourceNotFoundException("Part file", partId);
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PARTS_READ', 'DESIGN') or "
            + "@authorizationService.hasProjectPermission(authentication, #p0, 'PARTS_UPDATE', 'DESIGN')")
    public DesignFileRootsResponse listRoots(
            @P("projectId") Long projectId,
            Authentication authentication) {
        List<DesignFileRootResponse> roots = fileRootRepository.findByProjectIdOrderById(projectId).stream()
                .map(this::rootResponse)
                .toList();
        boolean canManage = authorizationService.hasProjectPermission(
                authentication, projectId, "PARTS_UPDATE", "DESIGN");
        return new DesignFileRootsResponse(roots, canManage);
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PARTS_UPDATE', 'DESIGN')")
    public DesignFileRootResponse addRoot(
            @P("projectId") Long projectId,
            DesignFileRootRequest request,
            Authentication authentication) {
        Projects project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        Path root = resolveRegisteredRoot(request.path());
        String path = root.toString();
        if (fileRootRepository.existsByProjectIdAndPathIgnoreCase(projectId, path)) {
            throw new IllegalArgumentException("This server folder is already registered for the project");
        }

        DesignFileRoot fileRoot = new DesignFileRoot();
        fileRoot.setProject(project);
        fileRoot.setPath(path);
        fileRoot.setCreatedBy(authentication.getName());
        fileRoot = fileRootRepository.save(fileRoot);
        auditService.record("DESIGN_FILE_ROOT_ADDED", "DESIGN_FILE_ROOT", fileRoot.getId(), fileRoot.getPath());
        return rootResponse(fileRoot);
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PARTS_UPDATE', 'DESIGN')")
    public void removeRoot(@P("projectId") Long projectId, Long rootId) {
        DesignFileRoot root = fileRootRepository.findByIdAndProjectId(rootId, projectId)
                .orElseThrow(() -> new DesignResourceNotFoundException("Server folder", rootId));
        auditService.record("DESIGN_FILE_ROOT_REMOVED", "DESIGN_FILE_ROOT", root.getId(), root.getPath());
        fileRootRepository.delete(root);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PARTS_READ', 'DESIGN') or "
            + "@authorizationService.hasProjectPermission(authentication, #p0, 'PARTS_UPDATE', 'DESIGN')")
    public DesignFileBrowserResponse browse(
            @P("projectId") Long projectId,
            Long rootId,
            String relativeDirectory) {
        List<DesignFileRoot> registeredRoots = fileRootRepository.findByProjectIdOrderById(projectId);
        DesignFileRoot selectedRoot = registeredRoots.stream()
                .filter(root -> root.getId().equals(rootId))
                .findFirst()
                .orElseThrow(() -> new DesignResourceNotFoundException("Server folder", rootId));
        Path root = resolveRegisteredRoot(selectedRoot.getPath());
        Path directory = resolveDirectory(root, relativeDirectory);
        List<DesignFileBrowserEntry> entries;
        try (Stream<Path> children = Files.list(directory)) {
            entries = children
                    .map(path -> browserEntry(path, root))
                    .filter(entry -> entry != null)
                    .sorted(Comparator.comparing(DesignFileBrowserEntry::directory).reversed()
                            .thenComparing(DesignFileBrowserEntry::name, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (IOException exception) {
            throw new DesignFileStorageNotConfiguredException();
        }

        String currentDirectory = relativePath(root, directory);
        String parentDirectory = directory.equals(root) ? null : relativePath(root, directory.getParent());
        List<DesignFileBrowserRoot> browserRoots = registeredRoots.stream()
                .map(fileRoot -> new DesignFileBrowserRoot(
                        fileRoot.getId(), rootName(Path.of(fileRoot.getPath())), fileRoot.getPath()))
                .toList();
        return new DesignFileBrowserResponse(
                browserRoots, selectedRoot.getId(), currentDirectory, parentDirectory, entries);
    }

    private boolean isUnderRoot(Path file, String configuredRoot) {
        try {
            Path root = Path.of(configuredRoot).toRealPath();
            if (!Files.isDirectory(root)) {
                throw new DesignFileStorageNotConfiguredException();
            }
            return file.startsWith(root);
        } catch (IOException | InvalidPathException exception) {
            throw new DesignFileStorageNotConfiguredException();
        }
    }

    private List<Path> configuredRoots() {
        List<Path> roots = Arrays.stream(allowedRootsConfiguration.split(";"))
                .map(String::trim)
                .filter(root -> !root.isEmpty())
                .map(Path::of)
                .toList();
        if (roots.isEmpty()) {
            throw new DesignFileStorageNotConfiguredException();
        }
        return roots;
    }

    private Path resolveRegisteredRoot(String requestedPath) {
        try {
            Path root = Path.of(requestedPath.trim()).toRealPath();
            if (!Files.isDirectory(root)) {
                throw new IllegalArgumentException("The selected server folder does not exist or is not a folder");
            }
            if (configuredRoots().stream().noneMatch(base -> isUnderRoot(root, base.toString()))) {
                throw new IllegalArgumentException("Choose a folder inside the configured server design-file location");
            }
            return root;
        } catch (IOException | InvalidPathException exception) {
            throw new IllegalArgumentException("The selected server folder does not exist or is not accessible");
        }
    }

    private Path resolveDirectory(Path root, String relativeDirectory) {
        try {
            Path relative = relativeDirectory == null || relativeDirectory.isBlank()
                    ? Path.of("")
                    : Path.of(relativeDirectory);
            if (relative.isAbsolute()) {
                throw new IllegalArgumentException("The selected folder is outside the configured server location");
            }
            Path directory = root.resolve(relative).normalize();
            if (!directory.startsWith(root)) {
                throw new IllegalArgumentException("The selected folder is outside the configured server location");
            }
            Path realDirectory = directory.toRealPath();
            if (!realDirectory.startsWith(root) || !Files.isDirectory(realDirectory)) {
                throw new IllegalArgumentException("The selected folder is outside the configured server location");
            }
            return realDirectory;
        } catch (IOException exception) {
            throw new DesignResourceNotFoundException("Server folder", relativeDirectory);
        } catch (InvalidPathException exception) {
            throw new IllegalArgumentException("The selected folder path is invalid");
        }
    }

    private DesignFileBrowserEntry browserEntry(Path path, Path root) {
        try {
            Path realPath = path.toRealPath();
            if (!realPath.startsWith(root)) {
                return null;
            }
            boolean directory = Files.isDirectory(realPath);
            if (!directory && !Files.isRegularFile(realPath)) {
                return null;
            }
            String relativePath = relativePath(root, realPath);
            return new DesignFileBrowserEntry(
                    path.getFileName().toString(),
                    relativePath,
                    directory,
                    directory ? null : realPath.toString());
        } catch (IOException exception) {
            return null;
        }
    }

    private String relativePath(Path root, Path path) {
        return root.relativize(path).toString();
    }

    private String rootName(Path root) {
        Path fileName = root.getFileName();
        return fileName == null ? root.toString() : fileName.toString();
    }

    private DesignFileRootResponse rootResponse(DesignFileRoot root) {
        return new DesignFileRootResponse(root.getId(), rootName(Path.of(root.getPath())), root.getPath());
    }
}
