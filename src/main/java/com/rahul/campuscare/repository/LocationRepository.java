package com.rahul.campuscare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahul.campuscare.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByActiveTrue();

    List<Location> findByBuilding(String building);

    List<Location> findByBuildingAndFloor(String building, Integer floor);

    Optional<Location> findByBuildingAndFloorAndWingAndRoomNumber(
            String building, Integer floor, String wing, String roomNumber);
}
