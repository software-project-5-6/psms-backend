package com.majstro.psms.backend.repository;

import com.majstro.psms.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    boolean existsByProjectName(String projectName);
}