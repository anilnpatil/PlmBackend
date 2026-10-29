package com.nextfirst.plm.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.nextfirst.plm.project.entity.Projects;

public interface ProjectRepository extends JpaRepository<Projects, Long>, JpaSpecificationExecutor<Projects> {

    boolean existsByProjectNumber(String projectNumber);

    boolean existsByProjectNumberAndIdNot(String projectNumber, Long id);
}
