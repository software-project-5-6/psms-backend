package com.majstro.psms.backend.repository;

import com.majstro.psms.backend.entity.Artifact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArtifactRepository extends JpaRepository<Artifact, Long> {
    List<Artifact> findByProject_Id(String projectId);
    Optional<Artifact> findByIdAndProject_Id(Long id, String projectId);
    void deleteByIdAndProject_Id(Long id, String projectId);
}
