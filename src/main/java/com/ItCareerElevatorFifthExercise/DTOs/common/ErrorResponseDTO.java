package com.ItCareerElevatorFifthExercise.DTOs.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {

    private Integer status;

    private String message;

    private Long timestamp;
}
