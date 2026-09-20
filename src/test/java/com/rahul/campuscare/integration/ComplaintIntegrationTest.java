package com.rahul.campuscare.integration;

import com.rahul.campuscare.entity.Complaint;
import com.rahul.campuscare.entity.ComplaintStatus;
import com.rahul.campuscare.entity.Role;
import com.rahul.campuscare.entity.User;
import com.rahul.campuscare.repository.ComplaintRepository;
import com.rahul.campuscare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies complaint management with real database interactions.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ComplaintIntegrationTest {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testStudent;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test student user
        testStudent = User.builder()
                .fullName("Test Student")
                .email("student@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.STUDENT)
                .build();
        testStudent = userRepository.save(testStudent);
    }

    @Test
    void complaintRepository_shouldSaveAndRetrieveComplaint() {
        // Arrange
        Complaint complaint = Complaint.builder()
                .title("Test Complaint")
                .description("This is a test complaint")
                .category("Infrastructure")
                .status(ComplaintStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .user(testStudent)
                .build();

        // Act
        Complaint savedComplaint = complaintRepository.save(complaint);
        Complaint foundComplaint = complaintRepository.findById(savedComplaint.getId()).orElse(null);

        // Assert
        assertThat(savedComplaint.getId()).isNotNull();
        assertThat(foundComplaint).isNotNull();
        assertThat(foundComplaint.getTitle()).isEqualTo("Test Complaint");
        assertThat(foundComplaint.getStatus()).isEqualTo(ComplaintStatus.PENDING);
        assertThat(foundComplaint.getUser().getId()).isEqualTo(testStudent.getId());
    }

    @Test
    void complaintRepository_shouldFindComplaintsByUser() {
        // Arrange
        Complaint complaint1 = Complaint.builder()
                .title("Complaint 1")
                .description("Description 1")
                .category("Infrastructure")
                .status(ComplaintStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .user(testStudent)
                .build();

        Complaint complaint2 = Complaint.builder()
                .title("Complaint 2")
                .description("Description 2")
                .category("Classroom")
                .status(ComplaintStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .user(testStudent)
                .build();

        complaintRepository.save(complaint1);
        complaintRepository.save(complaint2);

        // Act
        List<Complaint> userComplaints = complaintRepository.findByUser(testStudent);

        // Assert
        assertThat(userComplaints).hasSize(2);
        assertThat(userComplaints).extracting("title")
                .containsExactlyInAnyOrder("Complaint 1", "Complaint 2");
    }

    @Test
    void complaintRepository_shouldCountComplaintsByStatus() {
        // Arrange
        complaintRepository.save(createComplaint("Complaint 1", ComplaintStatus.PENDING));
        complaintRepository.save(createComplaint("Complaint 2", ComplaintStatus.PENDING));
        complaintRepository.save(createComplaint("Complaint 3", ComplaintStatus.IN_PROGRESS));
        complaintRepository.save(createComplaint("Complaint 4", ComplaintStatus.RESOLVED));
        complaintRepository.save(createComplaint("Complaint 5", ComplaintStatus.RESOLVED));
        complaintRepository.save(createComplaint("Complaint 6", ComplaintStatus.REJECTED));

        // Act
        long pendingCount = complaintRepository.countByStatus(ComplaintStatus.PENDING);
        long inProgressCount = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolvedCount = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long rejectedCount = complaintRepository.countByStatus(ComplaintStatus.REJECTED);
        long totalCount = complaintRepository.count();

        // Assert
        assertThat(pendingCount).isEqualTo(2);
        assertThat(inProgressCount).isEqualTo(1);
        assertThat(resolvedCount).isEqualTo(2);
        assertThat(rejectedCount).isEqualTo(1);
        assertThat(totalCount).isEqualTo(6);
    }

    @Test
    void complaintRepository_shouldUpdateComplaintStatus() {
        // Arrange
        Complaint complaint = createComplaint("Test Complaint", ComplaintStatus.PENDING);
        Complaint savedComplaint = complaintRepository.save(complaint);

        // Act
        savedComplaint.setStatus(ComplaintStatus.RESOLVED);
        savedComplaint.setAdminNote("Issue has been fixed");
        Complaint updatedComplaint = complaintRepository.save(savedComplaint);

        // Assert
        Complaint foundComplaint = complaintRepository.findById(updatedComplaint.getId()).orElse(null);
        assertThat(foundComplaint).isNotNull();
        assertThat(foundComplaint.getStatus()).isEqualTo(ComplaintStatus.RESOLVED);
        assertThat(foundComplaint.getAdminNote()).isEqualTo("Issue has been fixed");
    }

    @Test
    void complaintRepository_shouldHandleComplaintsWithoutImages() {
        // Arrange
        Complaint complaint = Complaint.builder()
                .title("No Image Complaint")
                .description("This complaint has no image")
                .category("Other")
                .status(ComplaintStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .imageUrl(null)
                .user(testStudent)
                .build();

        // Act
        Complaint savedComplaint = complaintRepository.save(complaint);

        // Assert
        assertThat(savedComplaint.getImageUrl()).isNull();
    }

    @Test
    void complaintRepository_shouldHandleComplaintsWithImages() {
        // Arrange
        Complaint complaint = Complaint.builder()
                .title("With Image Complaint")
                .description("This complaint has an image")
                .category("Infrastructure")
                .status(ComplaintStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .imageUrl("http://example.com/image.jpg")
                .user(testStudent)
                .build();

        // Act
        Complaint savedComplaint = complaintRepository.save(complaint);

        // Assert
        assertThat(savedComplaint.getImageUrl()).isEqualTo("http://example.com/image.jpg");
    }

    private Complaint createComplaint(String title, ComplaintStatus status) {
        return Complaint.builder()
                .title(title)
                .description("Description for " + title)
                .category("Infrastructure")
                .status(status)
                .createdAt(LocalDateTime.now())
                .user(testStudent)
                .build();
    }
}
