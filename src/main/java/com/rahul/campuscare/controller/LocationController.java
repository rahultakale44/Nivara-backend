package com.rahul.campuscare.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rahul.campuscare.dto.CreateLocationRequest;
import com.rahul.campuscare.dto.LocationResponse;
import com.rahul.campuscare.dto.UpdateLocationRequest;
import com.rahul.campuscare.service.LocationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody CreateLocationRequest request) {
        return ResponseEntity.ok(locationService.createLocation(request));
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getLocations(
            @RequestParam(required = false) String building,
            @RequestParam(required = false) Integer floor) {

        if (building != null && floor != null) {
            return ResponseEntity.ok(locationService.getLocationsByBuildingAndFloor(building, floor));
        } else if (building != null) {
            return ResponseEntity.ok(locationService.getLocationsByBuilding(building));
        } else {
            return ResponseEntity.ok(locationService.getActiveLocations());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(locationService.updateLocation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deactivateLocation(@PathVariable Long id) {
        locationService.deactivateLocation(id);
        return ResponseEntity.ok("Location deactivated successfully");
    }
}
