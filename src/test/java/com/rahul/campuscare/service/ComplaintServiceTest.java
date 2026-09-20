package com.rahul.campuscare.service;

import com.rahul.campuscare.dto.AdminStatsResponse;
import com.rahul.campuscare.dto.ComplaintResponse;
import com.rahul.campuscare.dto.CreateComplaintRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ComplaintService complaintService;

    private User testUser;
    private Location testLocation;
    private Complaint testComplaint;
    private CreateComplaintRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .password("password")
                .role(Role.STUDENT)
                .build();

        testLocation = Location.builder()
                .id(1L)
                .building("School of Computing")
                .floor(4)
                .wing("N")
                .roomNumber("N408")
                .displayName("School of Computing - N408")
                .active(true)
                .build();

        testComplaint = Complaint.builder()
                .id(1L)
                .title("Broken AC")
                .description("AC not working in Room 101")
                .category("Infrastructure")
                .status(ComplaintStatus.PENDING)
                .priority(Priority.MEDIUM)
                .location(testLocation)
                .createdAt(LocalDateTime.now())
                .user(testUser)
                .build();

        createRequest = new CreateComplaintRequest();
        createRequest.setTitle("Broken AC");
        createRequest.setDescription("AC not working in Room 101");
        createRequest.setCategory("Infrastructure");
        createRequest.setLocationId(1L);
    }

    private void mockSecurityContext(String email) {
        when(authentication.getName()).thenReturn(email);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createComplaint_shouldCreateComplaintSuccessfully() {
        // Arrange
        mockSecurityContext("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(testComplaint);

        // Act
        ComplaintResponse result = complaintService.createComplaint(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Broken AC");
        assertThat(result.getStatus()).isEqualTo(ComplaintStatus.PENDING);
        assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM);
        verify(userRepository).findByEmail("test@example.com");
        verify(locationRepository).findById(1L);
        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    void createComplaint_shouldSetPendingStatusByDefault() {
        // Arrange
        mockSecurityContext("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint savedComplaint = invocation.getArgument(0);
            assertThat(savedComplaint.getStatus()).isEqualTo(ComplaintStatus.PENDING);
            return savedComplaint;
        });

        // Act
        complaintService.createComplaint(createRequest);

        // Assert
        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    void createComplaint_shouldAssociateComplaintWithLoggedInUser() {
        // Arrange
        mockSecurityContext("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint savedComplaint = invocation.getArgument(0);
            assertThat(savedComplaint.getUser()).isEqualTo(testUser);
            return savedComplaint;
        });

        // Act
        complaintService.createComplaint(createRequest);

        // Assert
        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    void createComplaint_shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        mockSecurityContext("nonexistent@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> complaintService.createComplaint(createRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void createComplaint_shouldThrowExceptionWhenLocationNotFound() {
        // Arrange
        mockSecurityContext("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> complaintService.createComplaint(createRequest))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("Location not found");
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void createComplaint_shouldThrowExceptionWhenLocationInactive() {
        // Arrange
        testLocation.setActive(false);
        mockSecurityContext("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        // Act & Assert
        assertThatThrownBy(() -> complaintService.createComplaint(createRequest))
                .isInstanceOf(InactiveLocationException.class)
                .hasMessage("Cannot create complaint for inactive location");
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void getAllComplaints_shouldReturnAllComplaints() {
        // Arrange
        List<Complaint> complaints = Arrays.asList(testComplaint);
        when(complaintRepository.findAll()).thenReturn(complaints);

        // Act
        List<ComplaintResponse> result = complaintService.getAllComplaints();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Broken AC");
        verify(complaintRepository).findAll();
    }

    @Test
    void getComplaintById_shouldReturnComplaintWhenFound() {
        // Arrange
        mockSecurityContext("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(testComplaint));

        // Act
        ComplaintResponse result = complaintService.getComplaintById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Broken AC");
        verify(complaintRepository).findById(1L);
    }

    @Test
    void getComplaintById_shouldThrowExceptionWhenNotFound() {
        // Arrange
        when(complaintRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> complaintService.getComplaintById(999L))
                .isInstanceOf(ComplaintNotFoundException.class)
                .hasMessage("Complaint not found");
        verify(complaintRepository).findById(999L);
    }

    @Test
    void getComplaintById_studentCanAccessOwnComplaint() {
        // Arrange
        mockSecurityContext("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(testComplaint));

        // Act
        ComplaintResponse result = complaintService.getComplaintById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getReporterEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getComplaintById_studentCannotAccessAnotherStudentComplaint() {
        // Arrange
        User anotherStudent = User.builder()
                .id(2L)
                .fullName("Another Student")
                .email("another@example.com")
                .password("password")
                .role(Role.STUDENT)
                .build();

        Complaint anotherComplaint = Complaint.builder()
                .id(2L)
                .title("Another Complaint")
                .description("This belongs to another student")
                .category("Other")
                .status(ComplaintStatus.PENDING)
                .priority(Priority.MEDIUM)
                .location(testLocation)
                .createdAt(LocalDateTime.now())
                .user(anotherStudent)
                .build();

        mockSecurityContext("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(complaintRepository.findById(2L)).thenReturn(Optional.of(anotherComplaint));

        // Act & Assert - Student trying to access another student's complaint
        assertThatThrownBy(() -> complaintService.getComplaintById(2L))
                .isInstanceOf(ComplaintNotFoundException.class)
                .hasMessage("Complaint not found");
    }

    @Test
    void getComplaintById_adminCanAccessAnyComplaint() {
        // Arrange
        User admin = User.builder()
                .id(3L)
                .fullName("Admin User")
                .email("admin@example.com")
                .password("password")
                .role(Role.ADMIN)
                .build();

        mockSecurityContext("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(testComplaint));

        // Act
        ComplaintResponse result = complaintService.getComplaintById(1L);

        // Assert - Admin can access any student's complaint
        assertThat(result).isNotNull();
        assertThat(result.getReporterEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getMyComplaints_shouldReturnUserComplaints() {
        // Arrange
        mockSecurityContext("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(complaintRepository.findByUser(testUser)).thenReturn(Arrays.asList(testComplaint));

        // Act
        List<ComplaintResponse> result = complaintService.getMyComplaints();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReporterEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
        verify(complaintRepository).findByUser(testUser);
    }

    @Test
    void updateStatus_shouldUpdateComplaintStatus() {
        // Arrange
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(testComplaint));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(testComplaint);

        // Act
        ComplaintResponse result = complaintService.updateStatus(1L, ComplaintStatus.IN_PROGRESS, null);

        // Assert
        assertThat(result.getStatus()).isEqualTo(ComplaintStatus.IN_PROGRESS);
        verify(complaintRepository).findById(1L);
        verify(complaintRepository).save(testComplaint);
    }

    @Test
    void updateStatus_shouldUpdateComplaintStatusWithAdminNote() {
        // Arrange
        String adminNote = "Working on it";
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(testComplaint));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(testComplaint);

        // Act
        ComplaintResponse result = complaintService.updateStatus(1L, ComplaintStatus.IN_PROGRESS, adminNote);

        // Assert
        assertThat(result.getAdminNote()).isEqualTo("Working on it");
        verify(complaintRepository).save(testComplaint);
    }

    @Test
    void updateStatus_shouldNotSetAdminNoteWhenNull() {
        // Arrange
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(testComplaint));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(testComplaint);

        // Act
        complaintService.updateStatus(1L, ComplaintStatus.RESOLVED, null);

        // Assert
        // When adminNote is null, the service does not call setAdminNote
        // Verify that the complaint was saved
        verify(complaintRepository).save(testComplaint);
    }

    @Test
    void updateStatus_shouldThrowExceptionWhenComplaintNotFound() {
        // Arrange
        when(complaintRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> complaintService.updateStatus(999L, ComplaintStatus.RESOLVED, null))
                .isInstanceOf(ComplaintNotFoundException.class)
                .hasMessage("Complaint not found");
        verify(complaintRepository).findById(999L);
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void getAdminStats_shouldReturnCorrectStatistics() {
        // Arrange
        when(complaintRepository.count()).thenReturn(10L);
        when(complaintRepository.countByStatus(ComplaintStatus.PENDING)).thenReturn(3L);
        when(complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS)).thenReturn(2L);
        when(complaintRepository.countByStatus(ComplaintStatus.RESOLVED)).thenReturn(4L);
        when(complaintRepository.countByStatus(ComplaintStatus.REJECTED)).thenReturn(1L);

        // Act
        AdminStatsResponse stats = complaintService.getAdminStats();

        // Assert
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalComplaints()).isEqualTo(10L);
        assertThat(stats.getPendingComplaints()).isEqualTo(3L);
        assertThat(stats.getInProgressComplaints()).isEqualTo(2L);
        assertThat(stats.getResolvedComplaints()).isEqualTo(4L);
        assertThat(stats.getRejectedComplaints()).isEqualTo(1L);
        verify(complaintRepository).count();
        verify(complaintRepository).countByStatus(ComplaintStatus.PENDING);
        verify(complaintRepository).countByStatus(ComplaintStatus.IN_PROGRESS);
        verify(complaintRepository).countByStatus(ComplaintStatus.RESOLVED);
        verify(complaintRepository).countByStatus(ComplaintStatus.REJECTED);
    }

    @Test
    void getAdminStats_shouldHandleZeroComplaints() {
        // Arrange
        when(complaintRepository.count()).thenReturn(0L);
        when(complaintRepository.countByStatus(any(ComplaintStatus.class))).thenReturn(0L);

        // Act
        AdminStatsResponse stats = complaintService.getAdminStats();

        // Assert
        assertThat(stats.getTotalComplaints()).isEqualTo(0L);
        assertThat(stats.getPendingComplaints()).isEqualTo(0L);
        assertThat(stats.getInProgressComplaints()).isEqualTo(0L);
        assertThat(stats.getResolvedComplaints()).isEqualTo(0L);
        assertThat(stats.getRejectedComplaints()).isEqualTo(0L);
    }
}