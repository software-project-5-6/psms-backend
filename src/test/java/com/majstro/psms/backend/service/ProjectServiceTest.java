package com.majstro.psms.backend.service;

import com.majstro.psms.backend.dto.ProjectDto;
import com.majstro.psms.backend.dto.ProjectWithUsersDto;
import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.entity.ProjectUserRole;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.mapper.ProjectMapper;
import com.majstro.psms.backend.repository.ProjectRepository;
import com.majstro.psms.backend.repository.ProjectUserRoleRepository;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.impl.ProjectServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectUserRoleRepository projectUserRoleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void shouldCreateProject_Successfully() {
        // Arrange
        String userId = "user-1";
        ProjectDto inputDto = ProjectDto.builder().projectName("New Project").build();
        Project projectEntity = new Project();
        projectEntity.setProjectName("New Project");

        User creator = new User();
        creator.setId(userId);
        creator.setGlobalRole("APP_USER");

        when(projectRepository.existsByProjectName("New Project")).thenReturn(false);
        when(projectMapper.toEntity(inputDto)).thenReturn(projectEntity);
        when(projectRepository.save(any(Project.class))).thenReturn(projectEntity);
        when(userRepository.findById(userId)).thenReturn(Optional.of(creator));
        when(projectMapper.toDto(any(Project.class))).thenReturn(inputDto);

        // Act
        ProjectDto result = projectService.createProject(inputDto, userId);

        // Assert
        assertThat(result.getProjectName()).isEqualTo("New Project");
        verify(projectUserRoleRepository).save(any(ProjectUserRole.class)); // Verifies admin role was added
    }

    @Test
    void shouldThrowException_WhenProjectNameExists() {
        // Arrange
        ProjectDto inputDto = ProjectDto.builder().projectName("Existing Project").build();
        when(projectRepository.existsByProjectName("Existing Project")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                projectService.createProject(inputDto, "user-1")
        );

        verify(projectRepository, never()).save(any());
    }

    @Test
    void shouldGetProjectById() {
        // Arrange
        String projectId = "proj-1";
        Project project = new Project();
        project.setId(projectId);

        ProjectWithUsersDto expectedDto = new ProjectWithUsersDto();
        expectedDto.setId(projectId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMapper.toProjectWithUsersDto(project)).thenReturn(expectedDto);

        // Act
        ProjectWithUsersDto result = projectService.getProjectById(projectId);

        // Assert
        assertThat(result.getId()).isEqualTo(projectId);
    }

    @Test
    void shouldDeleteProject_WhenExists() {
        // Arrange
        String projectId = "proj-1";
        when(projectRepository.existsById(projectId)).thenReturn(true);

        // Act
        projectService.deleteProject(projectId);

        // Assert
        verify(projectRepository).deleteById(projectId);
    }

    @Test
    void shouldThrowException_WhenDeletingNonExistentProject() {
        // Arrange
        String projectId = "invalid-id";
        when(projectRepository.existsById(projectId)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                projectService.deleteProject(projectId)
        );
    }
}