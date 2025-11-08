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
    public ProjectDto createProject(ProjectDto projectDto, Long creatorUserId) {
        if (projectRepository.existsByProjectName(projectDto.getProjectName())) {
            throw new IllegalArgumentException("Project name already exists: " + projectDto.getProjectName());
        }

        // Create the project
        Project project = projectMapper.toEntity(projectDto);
        Project saved = projectRepository.save(project);

        // Automatically add the creator as ADMIN
        if (creatorUserId != null) {
            User creator = userRepository.findById(creatorUserId)
                    .orElseThrow(() -> new EntityNotFoundException("Creator user not found"));

            ProjectUserRole adminRole = ProjectUserRole.builder()
                    .project(saved)
                    .user(creator)
                    .role(ProjectRole.ADMIN)
                    .build();

            projectUserRoleRepository.save(adminRole);

            log.info("User {} created project {} and was added as ADMIN",
                creator.getEmail(), saved.getProjectName());
        }

        return projectMapper.toDto(saved);
    }

    @Override
    public ProjectWithUsersDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + id));
        return projectMapper.toProjectWithUsersDto(project);
    }

    @Override
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + id));

        existing.setProjectName(projectDto.getProjectName());
        existing.setDescription(projectDto.getDescription());
        existing.setClientName(projectDto.getClientName());
        existing.setClientEmail(projectDto.getClientEmail());
        existing.setClientPhone(projectDto.getClientPhone());
        existing.setIconUrl(projectDto.getIconUrl());
        existing.setPrice(projectDto.getPrice());
        existing.setArtifactCount(projectDto.getArtifactCount());

        Project updated = projectRepository.save(existing);
        return projectMapper.toDto(updated);
    }

    @Override
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new EntityNotFoundException("Project not found with ID: " + id);
        }
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void removeUserFromProject(Long projectId, Long userId) {
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