package com.ItCareerElevatorFifthExercise.exceptions;

import com.ItCareerElevatorFifthExercise.DTOs.common.ErrorResponseDTO;
import lombok.Getter;

@Getter
public class ApiGatewayException extends RuntimeException {

    private final Integer status;

    private final Long timestamp;

    public ApiGatewayException(ErrorResponseDTO errorResponseDTO) {
        super(errorResponseDTO.getMessage());
        this.status = errorResponseDTO.getStatus();
        this.timestamp = errorResponseDTO.getTimestamp();
    }
}
