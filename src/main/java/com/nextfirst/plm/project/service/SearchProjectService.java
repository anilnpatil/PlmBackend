package com.nextfirst.plm.project.service;

import com.nextfirst.plm.project.dto.ProjectPageResponse;
import com.nextfirst.plm.project.dto.ProjectSearchRequest;

public interface SearchProjectService {

    ProjectPageResponse searchProjects(ProjectSearchRequest request);
}
