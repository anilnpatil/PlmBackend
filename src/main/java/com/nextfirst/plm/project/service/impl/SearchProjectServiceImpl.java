package com.nextfirst.plm.project.service.impl;

import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextfirst.plm.project.dto.ProjectPageResponse;
import com.nextfirst.plm.project.dto.ProjectSearchRequest;
import com.nextfirst.plm.project.entity.Projects;
import com.nextfirst.plm.project.mapper.ProjectMapper;
import com.nextfirst.plm.project.repository.ProjectRepository;
import com.nextfirst.plm.project.service.SearchProjectService;
import com.nextfirst.plm.auth.entity.AuthPermission;
import com.nextfirst.plm.auth.entity.AuthRole;
import com.nextfirst.plm.auth.entity.ProjectAssignment;
import com.nextfirst.plm.auth.service.AuthorizationService;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Join;

@Service
public class SearchProjectServiceImpl implements SearchProjectService {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "id", "projectNumber", "projectName", "customerName",
            "targetStartDate", "targetEndDate", "createdAt");

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final AuthorizationService authorizationService;

    public SearchProjectServiceImpl(
            ProjectRepository projectRepository,
            ProjectMapper projectMapper,
            AuthorizationService authorizationService) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasAnyProjectPermission(authentication, 'PROJECT_READ')")
    public ProjectPageResponse searchProjects(ProjectSearchRequest request) {
        validateDateRanges(request);
        String sortBy = request.getSortBy();
        if (sortBy == null || !SORTABLE_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("sortBy must be one of: " + String.join(", ", SORTABLE_FIELDS));
        }

        String sortDirection = request.getSortDirection();
        if (sortDirection == null || sortDirection.isBlank()) {
            throw new IllegalArgumentException("sortDirection must be ASC or DESC");
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("sortDirection must be ASC or DESC", exception);
        }

        Sort sort = Sort.by(direction, sortBy);
        if (!"id".equals(sortBy)) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "id"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long visibleUserId = authorizationService.isSystemAdmin(authentication)
                ? null
                : authorizationService.currentUserId(authentication);
        Page<Projects> projects = projectRepository.findAll(
                buildSpecification(request, visibleUserId),
                PageRequest.of(request.getPage(), request.getSize(), sort));

        return new ProjectPageResponse(
                projects.map(projectMapper::toResponse).getContent(),
                projects.getNumber(),
                projects.getSize(),
                projects.getTotalElements(),
                projects.getTotalPages(),
                projects.isFirst(),
                projects.isLast());
    }

    private Specification<Projects> buildSpecification(ProjectSearchRequest request, Long visibleUserId) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (visibleUserId != null) {
                Subquery<Long> assignmentQuery = query.subquery(Long.class);
                Root<ProjectAssignment> assignment = assignmentQuery.from(ProjectAssignment.class);
                Join<ProjectAssignment, AuthRole> role = assignment.join("roles");
                Join<AuthRole, AuthPermission> permission = role.join("permissions");
                assignmentQuery.select(assignment.get("project").get("id"))
                        .where(
                                criteriaBuilder.equal(assignment.get("user").get("id"), visibleUserId),
                                criteriaBuilder.equal(assignment.get("project").get("id"), root.get("id")),
                                criteriaBuilder.equal(permission.get("code"), "PROJECT_READ"));
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.exists(assignmentQuery));
            }
            predicate = addContainsFilter(predicate, root, criteriaBuilder, "projectNumber", request.getProjectNumber());
            predicate = addContainsFilter(predicate, root, criteriaBuilder, "projectName", request.getProjectName());
            predicate = addContainsFilter(predicate, root, criteriaBuilder, "customerName", request.getCustomerName());

            if (request.getTargetStartDateFrom() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("targetStartDate"), request.getTargetStartDateFrom()));
            }
            if (request.getTargetStartDateTo() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("targetStartDate"), request.getTargetStartDateTo()));
            }
            if (request.getTargetEndDateFrom() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("targetEndDate"), request.getTargetEndDateFrom()));
            }
            if (request.getTargetEndDateTo() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("targetEndDate"), request.getTargetEndDateTo()));
            }
            return predicate;
        };
    }

    private Predicate addContainsFilter(
            Predicate predicate,
            jakarta.persistence.criteria.Root<Projects> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            String field,
            String value) {
        if (value == null || value.isBlank()) {
            return predicate;
        }
        String escapedValue = value.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .toLowerCase(Locale.ROOT);
        return criteriaBuilder.and(
                predicate,
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.<String>get(field)),
                        "%" + escapedValue + "%",
                        '\\'));
    }

    private void validateDateRanges(ProjectSearchRequest request) {
        if (request.getTargetStartDateFrom() != null
                && request.getTargetStartDateTo() != null
                && request.getTargetStartDateFrom().isAfter(request.getTargetStartDateTo())) {
            throw new IllegalArgumentException("targetStartDateFrom must not be after targetStartDateTo");
        }
        if (request.getTargetEndDateFrom() != null
                && request.getTargetEndDateTo() != null
                && request.getTargetEndDateFrom().isAfter(request.getTargetEndDateTo())) {
            throw new IllegalArgumentException("targetEndDateFrom must not be after targetEndDateTo");
        }
    }
}
