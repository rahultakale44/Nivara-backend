package com.rahul.campuscare.service;

import com.rahul.campuscare.dto.CreateLocationRequest;
import com.rahul.campuscare.dto.LocationResponse;
import com.rahul.campuscare.dto.UpdateLocationRequest;
import com.rahul.campuscare.entity.Location;
import com.rahul.campuscare.exception.DuplicateLocationException;
import com.rahul.campuscare.exception.LocationNotFoundException;
import com.rahul.campuscare.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

    private Location testLocation;
    private CreateLocationRequest createRequest;

    @BeforeEach
    void setUp() {
        testLocation = Location.builder()
                .id(1L)
                .building("School of Computing")
                .floor(4)
                .wing("N")
                .roomNumber("N408")
                .displayName("School of Computing - NN408")
                .active(true)
                .build();

        createRequest = new CreateLocationRequest();
        createRequest.setBuilding("School of Computing");
        createRequest.setFloor(4);
        createRequest.setWing("N");
        createRequest.setRoomNumber("N408");
    }

    @Test
    void createLocation_shouldCreateLocationSuccessfully() {
        // Arrange
        when(locationRepository.findByBuildingAndFloorAndWingAndRoomNumber(
                anyString(), anyInt(), anyString(), anyString())).thenReturn(Optional.empty());
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);

        // Act
        LocationResponse result = locationService.createLocation(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getBuilding()).isEqualTo("School of Computing");
        assertThat(result.getFloor()).isEqualTo(4);
        assertThat(result.getRoomNumber()).isEqualTo("N408");
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void createLocation_shouldThrowExceptionWhenDuplicateExists() {
        // Arrange
        when(locationRepository.findByBuildingAndFloorAndWingAndRoomNumber(
                anyString(), anyInt(), anyString(), anyString())).thenReturn(Optional.of(testLocation));

        // Act & Assert
        assertThatThrownBy(() -> locationService.createLocation(createRequest))
                .isInstanceOf(DuplicateLocationException.class)
                .hasMessage("Location already exists");
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void getLocationById_shouldReturnLocationWhenFound() {
        // Arrange
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        // Act
        LocationResponse result = locationService.getLocationById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(locationRepository).findById(1L);
    }

    @Test
    void getLocationById_shouldThrowExceptionWhenNotFound() {
        // Arrange
        when(locationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> locationService.getLocationById(999L))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("Location not found");
    }

    @Test
    void getActiveLocations_shouldReturnOnlyActiveLocations() {
        // Arrange
        List<Location> locations = Arrays.asList(testLocation);
        when(locationRepository.findByActiveTrue()).thenReturn(locations);

        // Act
        List<LocationResponse> result = locationService.getActiveLocations();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActive()).isTrue();
        verify(locationRepository).findByActiveTrue();
    }

    @Test
    void updateLocation_shouldUpdateLocationSuccessfully() {
        // Arrange
        UpdateLocationRequest updateRequest = new UpdateLocationRequest();
        updateRequest.setDisplayName("Updated Display Name");

        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);

        // Act
        LocationResponse result = locationService.updateLocation(1L, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(locationRepository).save(testLocation);
    }

    @Test
    void deactivateLocation_shouldSetActiveToFalse() {
        // Arrange
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);

        // Act
        locationService.deactivateLocation(1L);

        // Assert
        assertThat(testLocation.getActive()).isFalse();
        verify(locationRepository).save(testLocation);
    }
}
