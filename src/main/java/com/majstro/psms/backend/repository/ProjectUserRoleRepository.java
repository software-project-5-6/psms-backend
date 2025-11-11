package com.majstro.psms.backend.repository;

import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.entity.ProjectUserRole;
import com.majstro.psms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectUserRoleRepository extends JpaRepository<ProjectUserRole, Long> {

    // Find all users assigned to a project
    List<ProjectUserRole> findByProject(Project project);

    List<ProjectUserRole> findByUser(User user);// to find all projects for a user

    // Check if a user already belongs to a project
    Optional<ProjectUserRole> findByProjectAndUser(Project project, User user);

    // Check if a user is already a member of a project
    boolean existsByProjectAndUser(Project project, User user);

    // Optional: delete user from project
    void deleteByProjectAndUser(Project project, User user);
}