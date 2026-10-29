package com.nextfirst.plm.project.service;

import com.nextfirst.plm.project.dto.AddProjectRequest;
import com.nextfirst.plm.project.dto.ProjectResponse;

public interface AddProjectService {

    ProjectResponse addProject(AddProjectRequest request);
}
