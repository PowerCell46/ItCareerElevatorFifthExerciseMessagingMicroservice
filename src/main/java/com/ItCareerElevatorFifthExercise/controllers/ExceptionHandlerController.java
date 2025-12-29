package com.ItCareerElevatorFifthExercise.controllers;

import com.ItCareerElevatorFifthExercise.DTOs.common.ErrorResponseDTO;
import com.ItCareerElevatorFifthExercise.exceptions.ApiGatewayException;
import com.ItCareerElevatorFifthExercise.exceptions.UserPresenceMicroserviceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(UserPresenceMicroserviceException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserPresenceMicroserviceException(UserPresenceMicroserviceException ex) {
        log.warn("Handling UserPresenceMicroserviceException.");
        log.warn("Error status: {}, message: {}.", ex.getStatus(), ex.getMessage());

        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getStatus(),
                ex.getMessage(),
                ex.getTimestamp()
        );

        return ResponseEntity
                .status(ex.getStatus())
                .body(error);
    }

    @ExceptionHandler(ApiGatewayException.class)
    public ResponseEntity<ErrorResponseDTO> handleApiGatewayException(ApiGatewayException ex) {
        log.warn("Handling ApiGatewayException.");
        log.warn("Error status: {}, message: {}.", ex.getStatus(), ex.getMessage());

        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getStatus(),
                ex.getMessage(),
                ex.getTimestamp()
        );

        return ResponseEntity
                .status(ex.getStatus())
                .body(error);
    }
}
