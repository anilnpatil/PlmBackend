package com.nextfirst.plm.project.mapper;

import org.springframework.stereotype.Component;

import com.nextfirst.plm.project.dto.AddProjectRequest;
import com.nextfirst.plm.project.dto.ProjectResponse;
import com.nextfirst.plm.project.dto.UpdateProjectRequest;
import com.nextfirst.plm.project.entity.Projects;

@Component
public class ProjectMapper {

    public Projects toEntity(AddProjectRequest request) {
        Projects project = new Projects();
        project.setProjectNumber(request.projectNumber().trim());
        project.setProjectName(request.projectName().trim());
        project.setProjectDescription(trimToNull(request.projectDescription()));
        project.setCustomerName(request.customerName().trim());
        project.setCustomerAddress(trimToNull(request.customerAddress()));
        project.setTargetStartDate(request.targetStartDate());
        project.setTargetEndDate(request.targetEndDate());
        return project;
    }

    public void updateEntity(Projects project, UpdateProjectRequest request) {
        project.setProjectNumber(request.projectNumber().trim());
        project.setProjectName(request.projectName().trim());
        project.setProjectDescription(trimToNull(request.projectDescription()));
        project.setCustomerName(request.customerName().trim());
        project.setCustomerAddress(trimToNull(request.customerAddress()));
        project.setTargetStartDate(request.targetStartDate());
        project.setTargetEndDate(request.targetEndDate());
    }

    public ProjectResponse toResponse(Projects project) {
        return new ProjectResponse(
                project.getId(),
                project.getProjectNumber(),
                project.getProjectName(),
                project.getProjectDescription(),
                project.getCustomerName(),
                project.getCustomerAddress(),
                project.getTargetStartDate(),
                project.getTargetEndDate(),
                project.getCreatedAt());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
