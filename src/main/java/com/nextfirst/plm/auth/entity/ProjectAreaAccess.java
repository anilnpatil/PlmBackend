package com.nextfirst.plm.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "project_assignment_area_access",
        uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "area_id"}))
public class ProjectAreaAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private ProjectAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private FunctionalArea area;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false, length = 20)
    private AreaAccessLevel accessLevel;

    public Long getId() { return id; }
    public ProjectAssignment getAssignment() { return assignment; }
    public void setAssignment(ProjectAssignment assignment) { this.assignment = assignment; }
    public FunctionalArea getArea() { return area; }
    public void setArea(FunctionalArea area) { this.area = area; }
    public AreaAccessLevel getAccessLevel() { return accessLevel; }
    public void setAccessLevel(AreaAccessLevel accessLevel) { this.accessLevel = accessLevel; }
}
