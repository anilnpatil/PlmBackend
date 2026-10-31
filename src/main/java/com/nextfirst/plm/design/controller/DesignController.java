package com.nextfirst.plm.design.controller;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nextfirst.plm.design.dto.DesignMainAssemblyRequest;
import com.nextfirst.plm.design.dto.DesignMainAssemblyResponse;
import com.nextfirst.plm.design.dto.DesignFileBrowserResponse;
import com.nextfirst.plm.design.dto.DesignFileRootRequest;
import com.nextfirst.plm.design.dto.DesignFileRootResponse;
import com.nextfirst.plm.design.dto.DesignFileRootsResponse;
import com.nextfirst.plm.design.dto.DesignPartsRequest;
import com.nextfirst.plm.design.dto.DesignPartStatusRequest;
import com.nextfirst.plm.design.dto.DesignPartsResponse;
import com.nextfirst.plm.design.dto.DesignSubAssemblyRequest;
import com.nextfirst.plm.design.dto.DesignSubAssemblyResponse;
import com.nextfirst.plm.design.dto.DesignWorkspaceResponse;
import com.nextfirst.plm.design.service.DesignFileService;
import com.nextfirst.plm.design.service.DesignRegisterService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/designs")
public class DesignController {
    private final DesignRegisterService registerService;
    private final DesignFileService fileService;

    public DesignController(DesignRegisterService registerService, DesignFileService fileService) {
        this.registerService = registerService;
        this.fileService = fileService;
    }

    @GetMapping({"", "/workspace"})
    public DesignWorkspaceResponse workspace(
            @P("projectId") @PathVariable Long projectId, Authentication authentication) {
        return registerService.workspace(projectId, authentication);
    }

    @GetMapping("/parts/{partId}/file")
    public ResponseEntity<Resource> openPartFile(
            @P("projectId") @PathVariable Long projectId,
            @PathVariable Long partId) {
        return partFile(projectId, partId, false);
    }

    @GetMapping("/parts/{partId}/file/download")
    public ResponseEntity<Resource> downloadPartFile(
            @P("projectId") @PathVariable Long projectId,
            @PathVariable Long partId) {
        return partFile(projectId, partId, true);
    }

    @GetMapping("/file-roots")
    public DesignFileRootsResponse listFileRoots(
            @P("projectId") @PathVariable Long projectId,
            Authentication authentication) {
        return fileService.listRoots(projectId, authentication);
    }

    @PostMapping("/file-roots")
    @ResponseStatus(HttpStatus.CREATED)
    public DesignFileRootResponse addFileRoot(
            @P("projectId") @PathVariable Long projectId,
            @Valid @RequestBody DesignFileRootRequest request,
            Authentication authentication) {
        return fileService.addRoot(projectId, request, authentication);
    }

    @DeleteMapping("/file-roots/{rootId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFileRoot(
            @P("projectId") @PathVariable Long projectId,
            @PathVariable Long rootId) {
        fileService.removeRoot(projectId, rootId);
    }

    @GetMapping("/file-browser")
    public DesignFileBrowserResponse browseDesignFiles(
            @P("projectId") @PathVariable Long projectId,
            @RequestParam Long rootId,
            @RequestParam(defaultValue = "") String path) {
        return fileService.browse(projectId, rootId, path);
    }

    @PostMapping("/main-assemblies")
    @ResponseStatus(HttpStatus.CREATED)
    public DesignMainAssemblyResponse createMainAssembly(
            @P("projectId") @PathVariable Long projectId,
            @Valid @RequestBody DesignMainAssemblyRequest request,
            Authentication authentication) {
        return registerService.createMainAssembly(projectId, request, authentication);
    }

    @PostMapping("/main-assemblies/{mainAssemblyId}/sub-assemblies")
    @ResponseStatus(HttpStatus.CREATED)
    public DesignSubAssemblyResponse createSubAssembly(
            @P("projectId") @PathVariable Long projectId,
            @PathVariable Long mainAssemblyId,
            @Valid @RequestBody DesignSubAssemblyRequest request,
            Authentication authentication) {
        return registerService.createSubAssembly(projectId, mainAssemblyId, request, authentication);
    }

    @PostMapping("/parts")
    @ResponseStatus(HttpStatus.CREATED)
    public DesignPartsResponse createPart(
            @P("projectId") @PathVariable Long projectId,
            @Valid @RequestBody DesignPartsRequest request,
            Authentication authentication) {
        return registerService.createPart(projectId, request, authentication);
    }

    @PatchMapping("/parts/{partId}/status")
    public DesignPartsResponse updatePartStatus(
            @P("projectId") @PathVariable Long projectId,
            @PathVariable Long partId,
            @Valid @RequestBody DesignPartStatusRequest request,
            Authentication authentication) {
        return registerService.updatePartStatus(projectId, partId, request, authentication);
    }

    private ResponseEntity<Resource> partFile(Long projectId, Long partId, boolean download) {
        Path path = fileService.getPartFile(projectId, partId);
        FileSystemResource resource = new FileSystemResource(path);
        ContentDisposition disposition = (download
                ? ContentDisposition.attachment()
                : ContentDisposition.inline())
                .filename(path.getFileName().toString(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }
}
