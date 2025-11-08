package com.majstro.psms.backend.repository;

import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.entity.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {
    Optional<ProjectInvitation> findByToken(String token);

    List<ProjectInvitation> findByProjectAndEmail(Project project, String email);

    List<ProjectInvitation> findByProjectAndStatus(Project project, String status);
}

