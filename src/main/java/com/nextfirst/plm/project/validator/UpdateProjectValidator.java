package com.nextfirst.plm.project.validator;

import org.springframework.stereotype.Component;

import com.nextfirst.plm.project.dto.UpdateProjectRequest;
import com.nextfirst.plm.project.exception.DuplicateProjectNumberException;
import com.nextfirst.plm.project.repository.ProjectRepository;

@Component
public class UpdateProjectValidator {

    private final ProjectRepository projectRepository;

    public UpdateProjectValidator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void validate(Long id, UpdateProjectRequest request) {
        String projectNumber = request.projectNumber().trim();
        if (projectRepository.existsByProjectNumberAndIdNot(projectNumber, id)) {
            throw new DuplicateProjectNumberException(projectNumber);
        }
    }
}
