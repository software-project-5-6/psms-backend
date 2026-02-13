package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.dto.AskRequest;
import com.majstro.psms.backend.dto.AskResponse;
import com.majstro.psms.backend.dto.ProjectDto;
import com.majstro.psms.backend.dto.ProjectWithUsersDto;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.rag.RagServices;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.IProjectService;
import com.majstro.psms.backend.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final IProjectService projectService;
    private final IUserService userService;
    private final RagServices ragServices;

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(
            @RequestBody ProjectDto projectDto,
            @AuthenticationPrincipal Jwt jwt)
    {

        String creatorUserId = userService.getUserIdFromJwt(jwt);
        ProjectDto created = projectService.createProject(projectDto, creatorUserId);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectWithUsersDto> getProjectById(@PathVariable String id) {
        ProjectWithUsersDto project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @PostMapping("ask/{id}")
    public ResponseEntity<AskResponse> askProject(@PathVariable String id, @RequestBody AskRequest request) {
        String answer = ragServices.query(request.getQuestion(),id);

        return ResponseEntity.ok(new AskResponse(answer));
    }


    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        List<ProjectDto> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable String id, @RequestBody ProjectDto projectDto) {
        ProjectDto updated = projectService.updateProject(id, projectDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{projectId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromProject(
            @PathVariable String projectId,
            @PathVariable String userId) {
        projectService.removeUserFromProject(projectId, userId);
        return ResponseEntity.noContent().build();
    }

}