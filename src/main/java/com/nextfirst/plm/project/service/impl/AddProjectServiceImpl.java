package com.nextfirst.plm.project.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import com.nextfirst.plm.project.dto.AddProjectRequest;
import com.nextfirst.plm.project.dto.ProjectResponse;
import com.nextfirst.plm.project.mapper.ProjectMapper;
import com.nextfirst.plm.project.repository.ProjectRepository;
import com.nextfirst.plm.project.service.AddProjectService;
import com.nextfirst.plm.project.validator.AddProjectValidator;

@Service
public class AddProjectServiceImpl implements AddProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final AddProjectValidator addProjectValidator;

    public AddProjectServiceImpl(
            ProjectRepository projectRepository,
            ProjectMapper projectMapper,
            AddProjectValidator addProjectValidator) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.addProjectValidator = addProjectValidator;
    }

    @Override
    @Transactional
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'PROJECT_CREATE')")
    public ProjectResponse addProject(AddProjectRequest request) {
        addProjectValidator.validate(request);
        return projectMapper.toResponse(projectRepository.save(projectMapper.toEntity(request)));
    }
}
