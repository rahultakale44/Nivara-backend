package com.rahul.campuscare.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.rahul.campuscare.dto.AdminStatsResponse;
import com.rahul.campuscare.dto.ComplaintResponse;
import com.rahul.campuscare.dto.CreateComplaintRequest;
import com.rahul.campuscare.dto.LocationResponse;
import com.rahul.campuscare.entity.Complaint;
import com.rahul.campuscare.entity.ComplaintStatus;
import com.rahul.campuscare.entity.Location;
import com.rahul.campuscare.entity.Priority;
import com.rahul.campuscare.entity.Role;
import com.rahul.campuscare.entity.User;
import com.rahul.campuscare.exception.ComplaintNotFoundException;
import com.rahul.campuscare.exception.InactiveLocationException;
import com.rahul.campuscare.exception.LocationNotFoundException;
import com.rahul.campuscare.exception.UserNotFoundException;
import com.rahul.campuscare.repository.ComplaintRepository;
import com.rahul.campuscare.repository.LocationRepository;
import com.rahul.campuscare.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    public ComplaintResponse createComplaint(CreateComplaintRequest request) {

        User user = getLoggedInUser();

        // Validate location
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));

        if (!location.getActive()) {
            throw new InactiveLocationException("Cannot create complaint for inactive location");
        }

        // Set priority - default to MEDIUM if not provided
        Priority priority = request.getPriority() != null ? request.getPriority() : Priority.MEDIUM;

        Complaint complaint = Complaint.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .location(location)
                .priority(priority)
                .status(ComplaintStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        complaint = complaintRepository.save(complaint);
        return mapToResponse(complaint);
    }

    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ComplaintResponse getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        // SECURITY: Verify ownership - students can only view their own complaints
        User loggedInUser = getLoggedInUser();
        
        // If user is STUDENT, check ownership
        if (loggedInUser.getRole() == com.rahul.campuscare.entity.Role.STUDENT) {
            if (!complaint.getUser().getId().equals(loggedInUser.getId())) {
                throw new ComplaintNotFoundException("Complaint not found");
            }
        }
        // ADMIN can view all complaints
        
        return mapToResponse(complaint);
    }

    public List<ComplaintResponse> getMyComplaints() {

        User user = getLoggedInUser();

        return complaintRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ComplaintResponse updateStatus(Long id, ComplaintStatus status, String adminNote) {

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        complaint.setStatus(status);

        if (adminNote != null) {
            complaint.setAdminNote(adminNote);
        }

        complaint = complaintRepository.save(complaint);
        return mapToResponse(complaint);
    }

    public ComplaintResponse updatePriority(Long id, Priority priority) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        complaint.setPriority(priority);
        complaint = complaintRepository.save(complaint);
        return mapToResponse(complaint);
    }

    public AdminStatsResponse getAdminStats() {

        long total = complaintRepository.count();

        long pending = complaintRepository.countByStatus(ComplaintStatus.PENDING);

        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);

        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);

        long rejected = complaintRepository.countByStatus(ComplaintStatus.REJECTED);

        return new AdminStatsResponse(
                total,
                pending,
                inProgress,
                resolved,
                rejected
        );
    }

    private User getLoggedInUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private ComplaintResponse mapToResponse(Complaint complaint) {
        LocationResponse locationResponse = null;
        if (complaint.getLocation() != null) {
            Location loc = complaint.getLocation();
            locationResponse = LocationResponse.builder()
                    .id(loc.getId())
                    .building(loc.getBuilding())
                    .floor(loc.getFloor())
                    .wing(loc.getWing())
                    .roomNumber(loc.getRoomNumber())
                    .displayName(loc.getDisplayName())
                    .active(loc.getActive())
                    .build();
        }

        return ComplaintResponse.builder()
                .id(complaint.getId())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .category(complaint.getCategory())
                .status(complaint.getStatus())
                .priority(complaint.getPriority())
                .createdAt(complaint.getCreatedAt())
                .imageUrl(complaint.getImageUrl())
                .adminNote(complaint.getAdminNote())
                .location(locationResponse)
                .reporterName(complaint.getUser().getFullName())
                .reporterEmail(complaint.getUser().getEmail())
                .build();
    }
}