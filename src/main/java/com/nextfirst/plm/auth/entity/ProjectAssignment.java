package com.nextfirst.plm.auth.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.nextfirst.plm.project.entity.Projects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "project_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}),
        indexes = @Index(name = "idx_project_assignments_user", columnList = "user_id"))
public class ProjectAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Projects project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "project_assignment_roles",
            joinColumns = @JoinColumn(name = "assignment_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<AuthRole> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "project_assignment_areas",
            joinColumns = @JoinColumn(name = "assignment_id"),
            inverseJoinColumns = @JoinColumn(name = "area_id"))
    private Set<FunctionalArea> functionalAreas = new HashSet<>();

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @PrePersist
    void onCreate() {
        assignedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Projects getProject() { return project; }
    public void setProject(Projects project) { this.project = project; }
    public AuthUser getUser() { return user; }
    public void setUser(AuthUser user) { this.user = user; }
    public Set<AuthRole> getRoles() { return roles; }
    public Set<FunctionalArea> getFunctionalAreas() { return functionalAreas; }
    public Instant getAssignedAt() { return assignedAt; }
}
