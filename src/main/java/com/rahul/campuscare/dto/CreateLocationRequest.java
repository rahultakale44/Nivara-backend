package com.rahul.campuscare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateLocationRequest {

    @NotBlank(message = "Building is required")
    @Size(max = 100, message = "Building name must not exceed 100 characters")
    private String building;

    @NotNull(message = "Floor is required")
    @Min(value = 0, message = "Floor must be a positive number")
    private Integer floor;

    @Size(max = 10, message = "Wing must not exceed 10 characters")
    private String wing;

    @NotBlank(message = "Room number is required")
    @Size(max = 20, message = "Room number must not exceed 20 characters")
    private String roomNumber;

    @Size(max = 200, message = "Display name must not exceed 200 characters")
    private String displayName;
}
