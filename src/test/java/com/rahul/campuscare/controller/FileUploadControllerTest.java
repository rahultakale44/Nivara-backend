package com.rahul.campuscare.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void upload_shouldReturn401ForAnonymousUser() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void upload_shouldAcceptValidFileForAuthenticatedUser() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("http://localhost:8080/uploads/")));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void upload_shouldReturn400WhenFileIsEmpty() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/upload").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File is empty"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void upload_shouldReturn400WhenFileSizeExceedsLimit() throws Exception {
        // Arrange
        // Create a 6MB file (exceeds 5MB limit)
        byte[] largeFileContent = new byte[6 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeFileContent
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/upload").file(largeFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File size exceeds maximum limit of 5MB"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void upload_shouldWorkForAdminUsers() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "admin-upload.jpg",
                "image/jpeg",
                "admin test content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("http://localhost:8080/uploads/")));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void upload_shouldSanitizeFilename() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../../etc/passwd",
                "image/jpeg",
                "test content".getBytes()
        );

        // Act & Assert
        // Should not fail, filename should be sanitized
        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("http://localhost:8080/uploads/")));
    }
}
