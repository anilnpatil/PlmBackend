package com.nextfirst.plm.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nextfirst.plm.auth.entity.ProjectAssignment;

public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {
    Optional<ProjectAssignment> findByProjectIdAndUserId(Long projectId, Long userId);
    List<ProjectAssignment> findByProjectIdOrderByUserFullName(Long projectId);

    @Query("""
            select count(distinct assignment.id)
            from ProjectAssignment assignment
            join assignment.roles role
            join role.permissions permission
            where assignment.user.id = :userId
              and lower(permission.code) = lower(:permissionCode)
            """)
    long countByUserIdAndPermissionCode(
            @Param("userId") Long userId, @Param("permissionCode") String permissionCode);
}
