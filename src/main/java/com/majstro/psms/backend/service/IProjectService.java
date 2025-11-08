package com.majstro.psms.backend.service;

import com.majstro.psms.backend.dto.ProjectDto;
import com.majstro.psms.backend.dto.ProjectWithUsersDto;

import java.util.List;

public interface IProjectService {

    ProjectDto createProject(ProjectDto projectDto, Long creatorUserId);

    ProjectWithUsersDto getProjectById(Long id);

    List<ProjectDto> getAllProjects();

    ProjectDto updateProject(Long id, ProjectDto projectDto);

    void deleteProject(Long id);

    void removeUserFromProject(Long projectId, Long userId);
}