package com.nextfirst.plm.project.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import com.nextfirst.plm.project.entity.Projects;
import com.nextfirst.plm.project.exception.ProjectNotFoundException;
import com.nextfirst.plm.project.repository.ProjectRepository;
import com.nextfirst.plm.project.service.DeleteProjectService;

@Service
public class DeleteProjectServiceImpl implements DeleteProjectService {

    private final ProjectRepository projectRepository;

    public DeleteProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_DELETE', null)")
    public void deleteProject(Long id) {
        Projects project = projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        projectRepository.delete(project);
    }
}
