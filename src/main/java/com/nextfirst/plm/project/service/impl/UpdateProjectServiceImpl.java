package com.nextfirst.plm.project.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import com.nextfirst.plm.project.dto.ProjectResponse;
import com.nextfirst.plm.project.dto.UpdateProjectRequest;
import com.nextfirst.plm.project.entity.Projects;
import com.nextfirst.plm.project.exception.ProjectNotFoundException;
import com.nextfirst.plm.project.mapper.ProjectMapper;
import com.nextfirst.plm.project.repository.ProjectRepository;
import com.nextfirst.plm.project.service.UpdateProjectService;
import com.nextfirst.plm.project.validator.UpdateProjectValidator;

@Service
public class UpdateProjectServiceImpl implements UpdateProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UpdateProjectValidator updateProjectValidator;

    public UpdateProjectServiceImpl(
            ProjectRepository projectRepository,
            ProjectMapper projectMapper,
            UpdateProjectValidator updateProjectValidator) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.updateProjectValidator = updateProjectValidator;
    }

    @Override
    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_UPDATE', null)")
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Projects project = projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        updateProjectValidator.validate(id, request);
        projectMapper.updateEntity(project, request);
        return projectMapper.toResponse(project);
    }
}
