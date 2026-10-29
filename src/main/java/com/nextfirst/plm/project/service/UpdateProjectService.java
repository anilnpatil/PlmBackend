package com.nextfirst.plm.project.service;

import com.nextfirst.plm.project.dto.ProjectResponse;
import com.nextfirst.plm.project.dto.UpdateProjectRequest;

public interface UpdateProjectService {

    ProjectResponse updateProject(Long id, UpdateProjectRequest request);
}
