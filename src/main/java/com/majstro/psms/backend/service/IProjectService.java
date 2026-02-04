package com.majstro.psms.backend.service;

import com.majstro.psms.backend.dto.ProjectDto;
import com.majstro.psms.backend.dto.ProjectWithUsersDto;
import com.majstro.psms.backend.entity.Project;

import java.util.List;

public interface IProjectService {

    ProjectDto createProject(ProjectDto projectDto, String creatorUserId);

    ProjectWithUsersDto getProjectById(String id);

    List<ProjectDto> getAllProjects();

    ProjectDto updateProject(String id, ProjectDto projectDto);

    void deleteProject(String id);

    void removeUserFromProject(String projectId, String userId);

    Project getProjectEntityById(String id);
}