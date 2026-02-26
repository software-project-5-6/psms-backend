package com.majstro.psms.backend.service;

import com.majstro.psms.backend.dto.ProjectDto;
import com.majstro.psms.backend.dto.ProjectWithUsersDto;
import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.entity.ProjectRole;
import com.majstro.psms.backend.entity.ProjectUserRole;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.exception.ResourceAlreadyExistsException;
import com.majstro.psms.backend.mapper.ProjectMapper;
import com.majstro.psms.backend.repository.ProjectRepository;
import com.majstro.psms.backend.repository.ProjectUserRoleRepository;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.impl.ProjectServiceImpl;
import com.majstro.psms.backend.service.storage.FileStorageService;
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

    // ADDED: FileStorageService mock required for deleteProject
    @Mock
    private FileStorageService fileStorageService;

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
        verify(projectUserRoleRepository).save(any(ProjectUserRole.class));
    }

    @Test
    void shouldThrowException_WhenProjectNameExists() {
        // Arrange
        ProjectDto inputDto = ProjectDto.builder().projectName("Existing Project").build();
        when(projectRepository.existsByProjectName("Existing Project")).thenReturn(true);

        // Act & Assert - FIXED: Expecting ResourceAlreadyExistsException instead of IllegalArgumentException
        assertThrows(ResourceAlreadyExistsException.class, () ->
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
    void shouldGetAllProjects_ForRegularUser() {
        // Arrange
        String sub = "user-sub-123";
        User user = new User();
        user.setId("u1");

        Project p1 = new Project();
        p1.setProjectName("P1");
        ProjectUserRole role = new ProjectUserRole();
        role.setProject(p1);

        ProjectDto dto = ProjectDto.builder().projectName("P1").build();

        // Mock SecurityContext
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Jwt jwt = mock(Jwt.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn(sub);
        when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(List.of("APP_USER"));

        when(userRepository.findByCognitoSub(sub)).thenReturn(Optional.of(user));
        when(projectUserRoleRepository.findByUser(user)).thenReturn(List.of(role));
        when(projectMapper.toDto(p1)).thenReturn(dto);

        // Act
        List<ProjectDto> result = projectService.getAllProjects();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjectName()).isEqualTo("P1");
    }

    @Test
    void shouldUpdateProject() {
        // Arrange
        String projectId = "proj-1";
        Project existing = new Project();
        existing.setProjectName("Old Name");

        ProjectDto updateDto = ProjectDto.builder().projectName("New Name").build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(existing);
        when(projectMapper.toDto(existing)).thenReturn(updateDto);

        // Act
        ProjectDto result = projectService.updateProject(projectId, updateDto);

        // Assert
        assertThat(result.getProjectName()).isEqualTo("New Name");
        verify(projectRepository).save(existing);
    }

    @Test
    void shouldDeleteProject_WhenExists() throws Exception {
        // Arrange
        String projectId = "proj-1";
        when(projectRepository.existsById(projectId)).thenReturn(true);

        // Act
        projectService.deleteProject(projectId);

        // Assert
        verify(fileStorageService).deleteProjectDirectory(projectId);
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

    @Test
    void shouldRemoveUserFromProject() {
        // Arrange
        Project project = new Project();
        project.setId("p1");
        User user = new User();
        user.setId("u1");

        when(projectRepository.findById("p1")).thenReturn(Optional.of(project));
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(projectUserRoleRepository.existsByProjectAndUser(project, user)).thenReturn(true);

        // Act
        projectService.removeUserFromProject("p1", "u1");

        // Assert
        verify(projectUserRoleRepository).deleteByProjectAndUser(project, user);
    }

    @Test
    void shouldThrowException_WhenRemovingUserNotAssigned() {
        // Arrange
        Project project = new Project();
        project.setId("p1");
        User user = new User();
        user.setId("u1");

        when(projectRepository.findById("p1")).thenReturn(Optional.of(project));
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(projectUserRoleRepository.existsByProjectAndUser(project, user)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                projectService.removeUserFromProject("p1", "u1")
        );
    }
}