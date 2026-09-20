package com.rahul.campuscare.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rahul.campuscare.dto.CreateLocationRequest;
import com.rahul.campuscare.dto.LocationResponse;
import com.rahul.campuscare.dto.UpdateLocationRequest;
import com.rahul.campuscare.entity.Location;
import com.rahul.campuscare.exception.DuplicateLocationException;
import com.rahul.campuscare.exception.LocationNotFoundException;
import com.rahul.campuscare.repository.LocationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    @Transactional
    public LocationResponse createLocation(CreateLocationRequest request) {
        // Check for duplicate location
        Optional<Location> existing = locationRepository.findByBuildingAndFloorAndWingAndRoomNumber(
                request.getBuilding(),
                request.getFloor(),
                request.getWing(),
                request.getRoomNumber()
        );

        if (existing.isPresent()) {
            throw new DuplicateLocationException("Location already exists");
        }

        // Generate display name if not provided
        String displayName = request.getDisplayName();
        if (displayName == null || displayName.isBlank()) {
            displayName = generateDisplayName(
                    request.getBuilding(),
                    request.getFloor(),
                    request.getWing(),
                    request.getRoomNumber()
            );
        }

        Location location = Location.builder()
                .building(request.getBuilding())
                .floor(request.getFloor())
                .wing(request.getWing())
                .roomNumber(request.getRoomNumber())
                .displayName(displayName)
                .active(true)
                .build();

        location = locationRepository.save(location);
        return mapToResponse(location);
    }

    public LocationResponse getLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));
        return mapToResponse(location);
    }

    public List<LocationResponse> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LocationResponse> getActiveLocations() {
        return locationRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LocationResponse> getLocationsByBuilding(String building) {
        return locationRepository.findByBuilding(building).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LocationResponse> getLocationsByBuildingAndFloor(String building, Integer floor) {
        return locationRepository.findByBuildingAndFloor(building, floor).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LocationResponse updateLocation(Long id, UpdateLocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));

        // Check for duplicate if core fields are being changed
        if (request.getBuilding() != null || request.getFloor() != null ||
                request.getWing() != null || request.getRoomNumber() != null) {

            String newBuilding = request.getBuilding() != null ? request.getBuilding() : location.getBuilding();
            Integer newFloor = request.getFloor() != null ? request.getFloor() : location.getFloor();
            String newWing = request.getWing() != null ? request.getWing() : location.getWing();
            String newRoomNumber = request.getRoomNumber() != null ? request.getRoomNumber() : location.getRoomNumber();

            // Only check for duplicates if something actually changed
            if (!newBuilding.equals(location.getBuilding()) ||
                    !newFloor.equals(location.getFloor()) ||
                    !newWing.equals(location.getWing()) ||
                    !newRoomNumber.equals(location.getRoomNumber())) {

                Optional<Location> existing = locationRepository.findByBuildingAndFloorAndWingAndRoomNumber(
                        newBuilding, newFloor, newWing, newRoomNumber
                );

                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    throw new DuplicateLocationException("Location already exists");
                }
            }
        }

        // Update fields
        if (request.getBuilding() != null) {
            location.setBuilding(request.getBuilding());
        }
        if (request.getFloor() != null) {
            location.setFloor(request.getFloor());
        }
        if (request.getWing() != null) {
            location.setWing(request.getWing());
        }
        if (request.getRoomNumber() != null) {
            location.setRoomNumber(request.getRoomNumber());
        }
        if (request.getDisplayName() != null) {
            location.setDisplayName(request.getDisplayName());
        }
        if (request.getActive() != null) {
            location.setActive(request.getActive());
        }

        location = locationRepository.save(location);
        return mapToResponse(location);
    }

    @Transactional
    public void deactivateLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));
        location.setActive(false);
        locationRepository.save(location);
    }

    private String generateDisplayName(String building, Integer floor, String wing, String roomNumber) {
        StringBuilder sb = new StringBuilder(building);
        if (wing != null && !wing.isBlank()) {
            sb.append(" - ").append(wing);
        }
        sb.append(roomNumber);
        return sb.toString();
    }

    private LocationResponse mapToResponse(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .building(location.getBuilding())
                .floor(location.getFloor())
                .wing(location.getWing())
                .roomNumber(location.getRoomNumber())
                .displayName(location.getDisplayName())
                .active(location.getActive())
                .build();
    }
}
