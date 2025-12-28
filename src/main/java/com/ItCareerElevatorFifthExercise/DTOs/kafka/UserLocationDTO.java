package com.ItCareerElevatorFifthExercise.DTOs.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class UserLocationDTO {

    private String userId;

    private String username; // For reading data convenience

    private Double latitude;

    private Double longitude;

    private Long recordedAt;
}
