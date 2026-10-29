package com.nextfirst.plm.project.validator;

import org.springframework.stereotype.Component;

import com.nextfirst.plm.project.dto.AddProjectRequest;
import com.nextfirst.plm.project.exception.DuplicateProjectNumberException;
import com.nextfirst.plm.project.repository.ProjectRepository;

@Component
public class AddProjectValidator {

    private final ProjectRepository projectRepository;

    public AddProjectValidator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void validate(AddProjectRequest request) {
        if (projectRepository.existsByProjectNumber(request.projectNumber().trim())) {
            throw new DuplicateProjectNumberException(request.projectNumber().trim());
        }
    }
}
