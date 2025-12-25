package com.ItCareerElevatorFifthExercise.DTOs.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class LocationRequestDTO {

    private Double latitude;

    private Double longitude;

    private Long timestamp;
}
