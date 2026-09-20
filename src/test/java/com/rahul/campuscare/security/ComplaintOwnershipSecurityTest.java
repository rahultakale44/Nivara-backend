package com.rahul.campuscare.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.rahul.campuscare.entity.Complaint;
import com.rahul.campuscare.entity.ComplaintStatus;
import com.rahul.campuscare.entity.Role;
import com.rahul.campuscare.entity.User;
import com.rahul.campuscare.repository.ComplaintRepository;
import com.rahul.campuscare.repository.UserRepository;

/**
 * Security tests to ensure students can only access their own complaints
 * and admins can access all complaints (IDOR prevention)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ComplaintOwnershipSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    private User student1;
    private User student2;
    private User admin;
    private Complaint student1Complaint;
    private Complaint student2Complaint;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        student1 = User.builder()
                .fullName("Student One")
                .email("student1@example.com")
                .password("password")
                .role(Role.STUDENT)
                .build();
        userRepository.save(student1);

        student2 = User.builder()
                .fullName("Student Two")
                .email("student2@example.com")
                .password("password")
                .role(Role.STUDENT)
                .build();
        userRepository.save(student2);

        admin = User.builder()
                .fullName("Admin User")
                .email("admin@example.com")
                .password("password")
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        // Create complaints for each student
        student1Complaint = Complaint.builder()
                .title("Student 1 Complaint")
                .description("This is student 1's private complaint")
                .category("Infrastructure")
                .status(ComplaintStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .user(student1)
                .build();
        complaintRepository.save(student1Complaint);

        student2Complaint = Complaint.builder()
                .title("Student 2 Complaint")
                .description("This is student 2's private complaint")
                .category("Facilities")
                .status(ComplaintStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .user(student2)
                .build();
        complaintRepository.save(student2Complaint);
    }

    @Test
    @WithMockUser(username = "student1@example.com", roles = {"STUDENT"})
    void student_canAccessOwnComplaint() throws Exception {
        mockMvc.perform(get("/api/complaints/" + student1Complaint.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Student 1 Complaint"));
    }

    @Test
    @WithMockUser(username = "student1@example.com", roles = {"STUDENT"})
    void student_cannotAccessAnotherStudentComplaint() throws Exception {
        // IDOR attack attempt: Student 1 tries to access Student 2's complaint
        mockMvc.perform(get("/api/complaints/" + student2Complaint.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Complaint not found"));
    }

    @Test
    @WithMockUser(username = "student2@example.com", roles = {"STUDENT"})
    void anotherStudent_cannotAccessOtherStudentComplaint() throws Exception {
        // IDOR attack attempt: Student 2 tries to access Student 1's complaint
        mockMvc.perform(get("/api/complaints/" + student1Complaint.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Complaint not found"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void admin_canAccessAllComplaints() throws Exception {
        // Admin can access student 1's complaint
        mockMvc.perform(get("/api/complaints/" + student1Complaint.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Student 1 Complaint"));

        // Admin can access student 2's complaint
        mockMvc.perform(get("/api/complaints/" + student2Complaint.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Student 2 Complaint"));
    }

    @Test
    void anonymous_cannotAccessAnyComplaint() throws Exception {
        mockMvc.perform(get("/api/complaints/" + student1Complaint.getId()))
                .andExpect(status().isUnauthorized());
    }
}
