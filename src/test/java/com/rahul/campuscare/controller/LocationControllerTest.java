package com.rahul.campuscare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.campuscare.dto.CreateLocationRequest;
import com.rahul.campuscare.dto.LocationResponse;
import com.rahul.campuscare.service.LocationService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LocationService locationService;

    private LocationResponse testLocationResponse;

    @BeforeEach
    void setUp() {
        testLocationResponse = LocationResponse.builder()
                .id(1L)
                .building("School of Computing")
                .floor(4)
                .wing("N")
                .roomNumber("N408")
                .displayName("School of Computing - NN408")
                .active(true)
                .build();
    }

    @Test
    void getLocations_shouldReturn401UnauthorizedForAnonymousUser() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void getLocations_shouldReturnLocationsForStudent() throws Exception {
        // Arrange
        List<LocationResponse> locations = Arrays.asList(testLocationResponse);
        when(locationService.getActiveLocations()).thenReturn(locations);

        // Act & Assert
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].building").value("School of Computing"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void createLocation_shouldBeForbiddenForStudent() throws Exception {
        // Arrange
        CreateLocationRequest request = new CreateLocationRequest();
        request.setBuilding("School of Computing");
        request.setFloor(4);
        request.setWing("N");
        request.setRoomNumber("N408");

        // Act & Assert
        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void createLocation_shouldCreateLocationForAdmin() throws Exception {
        // Arrange
        CreateLocationRequest request = new CreateLocationRequest();
        request.setBuilding("School of Computing");
        request.setFloor(4);
        request.setWing("N");
        request.setRoomNumber("N408");

        when(locationService.createLocation(any(CreateLocationRequest.class)))
                .thenReturn(testLocationResponse);

        // Act & Assert
        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.building").value("School of Computing"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void updateLocation_shouldBeForbiddenForStudent() throws Exception {
        // Arrange
        CreateLocationRequest request = new CreateLocationRequest();
        request.setBuilding("Updated Building");

        // Act & Assert
        mockMvc.perform(put("/api/locations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void deleteLocation_shouldBeForbiddenForStudent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/locations/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteLocation_shouldDeactivateForAdmin() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/locations/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Location deactivated successfully"));
    }
}
