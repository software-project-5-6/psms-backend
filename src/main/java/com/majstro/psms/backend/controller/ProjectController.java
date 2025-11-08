package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.dto.ProjectDto;
import com.majstro.psms.backend.dto.ProjectWithUsersDto;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final IProjectService projectService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(
            @RequestBody ProjectDto projectDto,
            @AuthenticationPrincipal Jwt jwt) {

        Long creatorUserId = getUserIdFromJwt(jwt);
        ProjectDto created = projectService.createProject(projectDto, creatorUserId);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectWithUsersDto> getProjectById(@PathVariable Long id) {
        ProjectWithUsersDto project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        List<ProjectDto> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long id, @RequestBody ProjectDto projectDto) {
        ProjectDto updated = projectService.updateProject(id, projectDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{projectId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromProject(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        projectService.removeUserFromProject(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromJwt(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        String cognitoSub = jwt.getClaimAsString("sub");
        if (cognitoSub != null) {
            User user = userRepository.findByCognitoSub(cognitoSub)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return user.getId();
        }
        return null;
    }
}