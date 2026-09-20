package com.rahul.campuscare.dto;

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
public class LocationResponse {

    private Long id;
    private String building;
    private Integer floor;
    private String wing;
    private String roomNumber;
    private String displayName;
    private Boolean active;
}
