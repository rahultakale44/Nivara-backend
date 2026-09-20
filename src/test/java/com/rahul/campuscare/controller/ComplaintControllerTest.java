package com.rahul.campuscare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.campuscare.dto.ComplaintResponse;
import com.rahul.campuscare.dto.CreateComplaintRequest;
import com.rahul.campuscare.dto.LocationResponse;
import com.rahul.campuscare.dto.UpdateComplaintStatusRequest;
import com.rahul.campuscare.entity.ComplaintStatus;
import com.rahul.campuscare.entity.Priority;
import com.rahul.campuscare.service.ComplaintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComplaintService complaintService;

    private LocationResponse testLocationResponse;
    private ComplaintResponse testComplaintResponse;

    @BeforeEach
    void setUp() {
        testLocationResponse = LocationResponse.builder()
                .id(1L)
                .building("School of Computing")
                .floor(4)
                .wing("N")
                .roomNumber("N408")
                .displayName("School of Computing - N408")
                .active(true)
                .build();

        testComplaintResponse = ComplaintResponse.builder()
                .id(1L)
                .title("Broken AC")
                .description("AC not working")
                .category("Infrastructure")
                .status(ComplaintStatus.PENDING)
                .priority(Priority.MEDIUM)
                .location(testLocationResponse)
                .createdAt(LocalDateTime.now())
                .reporterName("Test User")
                .reporterEmail("test@example.com")
                .build();
    }

    @Test
    void createComplaint_shouldReturn401UnauthorizedForAnonymousUser() throws Exception {
        // Arrange
        CreateComplaintRequest request = new CreateComplaintRequest();
        request.setTitle("Test Complaint");
        request.setDescription("Test Description");
        request.setCategory("Infrastructure");
        request.setLocationId(1L);

        // Act & Assert
        // With UnauthorizedEntryPoint, anonymous users now get 401
        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void createComplaint_shouldCreateComplaintWithStudentRole() throws Exception {
        // Arrange
        CreateComplaintRequest request = new CreateComplaintRequest();
        request.setTitle("Broken AC");
        request.setDescription("AC not working");
        request.setCategory("Infrastructure");
        request.setLocationId(1L);

        when(complaintService.createComplaint(any(CreateComplaintRequest.class)))
                .thenReturn(testComplaintResponse);

        // Act & Assert
        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Broken AC"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void createComplaint_shouldBeForbiddenForAdmin() throws Exception {
        // Arrange
        CreateComplaintRequest request = new CreateComplaintRequest();
        request.setTitle("Test Complaint");
        request.setDescription("Test Description");
        request.setCategory("Infrastructure");
        request.setLocationId(1L);

        // Act & Assert
        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyComplaints_shouldReturn401UnauthorizedForAnonymousUser() throws Exception {
        // Act & Assert
        // With UnauthorizedEntryPoint, anonymous users now get 401
        mockMvc.perform(get("/api/complaints/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void getMyComplaints_shouldReturnUserComplaints() throws Exception {
        // Arrange
        List<ComplaintResponse> complaints = Arrays.asList(testComplaintResponse);
        when(complaintService.getMyComplaints()).thenReturn(complaints);

        // Act & Assert
        mockMvc.perform(get("/api/complaints/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Broken AC"));
    }

    @Test
    void getAllComplaints_shouldReturn401UnauthorizedForAnonymousUser() throws Exception {
        // Act & Assert
        // With UnauthorizedEntryPoint, anonymous users now get 401
        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void getAllComplaints_shouldBeForbiddenForStudent() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getAllComplaints_shouldReturnAllComplaintsForAdmin() throws Exception {
        // Arrange
        List<ComplaintResponse> complaints = Arrays.asList(testComplaintResponse);
        when(complaintService.getAllComplaints()).thenReturn(complaints);

        // Act & Assert
        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Broken AC"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void getComplaintById_shouldReturnComplaint() throws Exception {
        // Arrange
        when(complaintService.getComplaintById(1L)).thenReturn(testComplaintResponse);

        // Act & Assert
        mockMvc.perform(get("/api/complaints/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Broken AC"));
    }

    @Test
    void updateStatus_shouldReturn401UnauthorizedForAnonymousUser() throws Exception {
        // Arrange
        UpdateComplaintStatusRequest request = new UpdateComplaintStatusRequest();
        request.setStatus(ComplaintStatus.IN_PROGRESS);

        // Act & Assert
        // With UnauthorizedEntryPoint, anonymous users now get 401
        mockMvc.perform(put("/api/complaints/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void updateStatus_shouldBeForbiddenForStudent() throws Exception {
        // Arrange
        UpdateComplaintStatusRequest request = new UpdateComplaintStatusRequest();
        request.setStatus(ComplaintStatus.IN_PROGRESS);

        // Act & Assert
        mockMvc.perform(put("/api/complaints/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void updateStatus_shouldUpdateStatusForAdmin() throws Exception {
        // Arrange
        UpdateComplaintStatusRequest request = new UpdateComplaintStatusRequest();
        request.setStatus(ComplaintStatus.IN_PROGRESS);
        request.setAdminNote("Working on it");

        ComplaintResponse updatedComplaintResponse = ComplaintResponse.builder()
                .id(1L)
                .title("Broken AC")
                .description("AC not working")
                .category("Infrastructure")
                .status(ComplaintStatus.IN_PROGRESS)
                .priority(Priority.MEDIUM)
                .adminNote("Working on it")
                .location(testLocationResponse)
                .createdAt(LocalDateTime.now())
                .reporterName("Test User")
                .reporterEmail("test@example.com")
                .build();

        when(complaintService.updateStatus(eq(1L), eq(ComplaintStatus.IN_PROGRESS), eq("Working on it")))
                .thenReturn(updatedComplaintResponse);

        // Act & Assert
        mockMvc.perform(put("/api/complaints/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.adminNote").value("Working on it"));
    }
}
