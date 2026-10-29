package com.nextfirst.plm.project.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.nextfirst.plm.project.dto.AddProjectRequest;
import com.nextfirst.plm.project.dto.ProjectPageResponse;
import com.nextfirst.plm.project.dto.ProjectResponse;
import com.nextfirst.plm.project.dto.ProjectSearchRequest;
import com.nextfirst.plm.project.dto.UpdateProjectRequest;
import com.nextfirst.plm.project.service.AddProjectService;
import com.nextfirst.plm.project.service.DeleteProjectService;
import com.nextfirst.plm.project.service.GetProjectService;
import com.nextfirst.plm.project.service.SearchProjectService;
import com.nextfirst.plm.project.service.UpdateProjectService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final AddProjectService addProjectService;
    private final UpdateProjectService updateProjectService;
    private final DeleteProjectService deleteProjectService;
    private final GetProjectService getProjectService;
    private final SearchProjectService searchProjectService;

    public ProjectController(
            AddProjectService addProjectService,
            UpdateProjectService updateProjectService,
            DeleteProjectService deleteProjectService,
            GetProjectService getProjectService,
            SearchProjectService searchProjectService) {
        this.addProjectService = addProjectService;
        this.updateProjectService = updateProjectService;
        this.deleteProjectService = deleteProjectService;
        this.getProjectService = getProjectService;
        this.searchProjectService = searchProjectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> addProject(@Valid @RequestBody AddProjectRequest request) {
        ProjectResponse response = addProjectService.addProject(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(updateProjectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        deleteProjectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(getProjectService.getProject(id));
    }

    @GetMapping
    public ResponseEntity<ProjectPageResponse> searchProjects(
            @Valid @ModelAttribute ProjectSearchRequest request) {
        return ResponseEntity.ok(searchProjectService.searchProjects(request));
    }
}
