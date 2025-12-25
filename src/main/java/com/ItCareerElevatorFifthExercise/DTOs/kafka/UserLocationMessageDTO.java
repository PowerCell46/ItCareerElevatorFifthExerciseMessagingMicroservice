package com.ItCareerElevatorFifthExercise.DTOs.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class UserLocationMessageDTO {

    private String userId;

    private Double latitude;

    private Double longitude;

    private Long timestamp;
}
