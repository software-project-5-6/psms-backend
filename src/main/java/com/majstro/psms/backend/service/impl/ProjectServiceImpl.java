package com.majstro.psms.backend.service.impl;

import com.majstro.psms.backend.dto.ProjectDto;
import com.majstro.psms.backend.dto.ProjectWithUsersDto;
import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.entity.ProjectRole;
import com.majstro.psms.backend.entity.ProjectUserRole;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.mapper.ProjectMapper;
import com.majstro.psms.backend.repository.ProjectRepository;
import com.majstro.psms.backend.repository.ProjectUserRoleRepository;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.IProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements IProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProjectDto createProject(ProjectDto projectDto, String creatorUserId) {
        if (projectRepository.existsByProjectName(projectDto.getProjectName())) {
            throw new IllegalArgumentException("Project name already exists: " + projectDto.getProjectName());
        }

        // Create the project
        Project project = projectMapper.toEntity(projectDto);
        Project saved = projectRepository.save(project);

        // Automatically add the creator as ADMIN (but NOT if they are APP_ADMIN)
        // APP_ADMIN has global access and doesn't need to be added to individual projects
        if (creatorUserId != null) {
            User creator = userRepository.findById(creatorUserId)
                    .orElseThrow(() -> new EntityNotFoundException("Creator user not found"));

            // Only add to project if user is NOT APP_ADMIN
            if (!"APP_ADMIN".equals(creator.getGlobalRole())) {
                ProjectUserRole adminRole = ProjectUserRole.builder()
                        .project(saved)
                        .user(creator)
                        .role(ProjectRole.ADMIN)
                        .build();

                projectUserRoleRepository.save(adminRole);

                log.info("User {} created project {} and was added as ADMIN",
                    creator.getEmail(), saved.getProjectName());
            } else {
                log.info("APP_ADMIN user {} created project {} (not added to team - has global access)",
                    creator.getEmail(), saved.getProjectName());
            }
        }

        return projectMapper.toDto(saved);
    }

    @Override
    public ProjectWithUsersDto getProjectById(String id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + id));
        return projectMapper.toProjectWithUsersDto(project);
    }

    @Override
    public Project getProjectEntityById(String id) {
        return projectRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Project not found with ID: " + id));
    }

    @Override
    public List<ProjectDto> getAllProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();

        String userId = jwt.getClaimAsString("sub");
        List<String> groups = jwt.getClaimAsStringList("cognito:groups");

        User user = userRepository.findByCognitoSub(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with sub: " + userId));

        List<Project> projects;

        if (groups != null && groups.contains("APP_ADMIN")) {
            projects = projectRepository.findAll();
        } else {
            projects = projectUserRoleRepository.findByUser(user)
                    .stream()
                    .map(ProjectUserRole::getProject)
                    .distinct()
                    .collect(Collectors.toList());
        }

        return projects.stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectDto updateProject(String id, ProjectDto projectDto) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + id));

        existing.setProjectName(projectDto.getProjectName());
        existing.setDescription(projectDto.getDescription());
        existing.setClientName(projectDto.getClientName());
        existing.setClientEmail(projectDto.getClientEmail());
        existing.setClientPhone(projectDto.getClientPhone());
        existing.setIconUrl(projectDto.getIconUrl());
        existing.setArtifactCount(projectDto.getArtifactCount());

        Project updated = projectRepository.save(existing);
        return projectMapper.toDto(updated);
    }

    @Override
    public void deleteProject(String id) {
        if (!projectRepository.existsById(id)) {
            throw new EntityNotFoundException("Project not found with ID: " + id);
        }
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void removeUserFromProject(String projectId, String userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + projectId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        
        // Check if the user is actually assigned to the project
        if (!projectUserRoleRepository.existsByProjectAndUser(project, user)) {
            throw new IllegalArgumentException("User is not a member of this project");
        }
        
        projectUserRoleRepository.deleteByProjectAndUser(project, user);
    }
}