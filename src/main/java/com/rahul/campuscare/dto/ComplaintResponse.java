package com.rahul.campuscare.dto;

import java.time.LocalDateTime;

import com.rahul.campuscare.entity.ComplaintStatus;
import com.rahul.campuscare.entity.Priority;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private ComplaintStatus status;
    private Priority priority;
    private LocalDateTime createdAt;
    private String imageUrl;
    private String adminNote;
    private LocationResponse location;
    private String reporterName;
    private String reporterEmail;
}
