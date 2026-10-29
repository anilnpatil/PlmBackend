package com.nextfirst.plm.project.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import com.nextfirst.plm.project.dto.ProjectResponse;
import com.nextfirst.plm.project.exception.ProjectNotFoundException;
import com.nextfirst.plm.project.mapper.ProjectMapper;
import com.nextfirst.plm.project.repository.ProjectRepository;
import com.nextfirst.plm.project.service.GetProjectService;

@Service
public class GetProjectServiceImpl implements GetProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public GetProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_READ', null)")
    public ProjectResponse getProject(Long id) {
        return projectRepository.findById(id)
                .map(projectMapper::toResponse)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }
}
