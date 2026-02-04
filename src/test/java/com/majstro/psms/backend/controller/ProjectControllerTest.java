package com.majstro.psms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.majstro.psms.backend.dto.ProjectDto;
import com.majstro.psms.backend.dto.ProjectWithUsersDto;
import com.majstro.psms.backend.service.IProjectService;
import com.majstro.psms.backend.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IProjectService projectService;

    @MockBean
    private IUserService userService;

    @Test
    void shouldCreateProject_WhenValidRequest() throws Exception {
        // Arrange
        // FIX: Using Builder pattern as defined in your DTO
        ProjectDto requestDto = ProjectDto.builder()
                .id("1")
                .projectName("New Project") // Correct field name
                .description("Desc")
                .build();

        String fakeUserId = "user-123";

        given(userService.getUserIdFromJwt(any())).willReturn(fakeUserId);
        given(projectService.createProject(any(ProjectDto.class), eq(fakeUserId))).willReturn(requestDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isOk())
                // FIX: Verify "projectName" instead of "name"
                .andExpect(jsonPath("$.projectName").value("New Project"));
    }

    @Test
    void shouldGetProjectById() throws Exception {
        // Arrange
        // Assuming ProjectWithUsersDto also has a NoArgsConstructor or Builder.
        // If not, you might need to adjust this similarly to ProjectDto.
        ProjectWithUsersDto responseDto = new ProjectWithUsersDto();
        responseDto.setId("1");
        responseDto.setProjectName("Project A");

        given(projectService.getProjectById("1")).willReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/projects/1")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.projectName").value("Project A"));
    }

    @Test
    void shouldGetAllProjects() throws Exception {
        // Arrange
        ProjectDto p1 = ProjectDto.builder()
                .id("1")
                .projectName("P1")
                .build();

        given(projectService.getAllProjects()).willReturn(List.of(p1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/projects")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].projectName").value("P1"));
    }

    @Test
    void shouldDeleteProject() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/projects/1")
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(projectService).deleteProject("1");
    }
}