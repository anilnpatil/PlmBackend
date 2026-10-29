
package com.nextfirst.plm.project.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.PrePersist;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Projects {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_number", nullable = false, unique = true, length = 30)
    private String projectNumber;

    @Column(name = "project_name", nullable = false, length = 30)
    private String projectName;

    @Column(name = "project_description", length = 2000)
    private String projectDescription;

    @Column(name = "customer_name", nullable = false, length = 30)
    private String customerName;

    @Column(name = "customer_address", length = 100)
    private String customerAddress;

    private LocalDate targetStartDate;

    private LocalDate targetEndDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void setCreatedAtIfMissing() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
