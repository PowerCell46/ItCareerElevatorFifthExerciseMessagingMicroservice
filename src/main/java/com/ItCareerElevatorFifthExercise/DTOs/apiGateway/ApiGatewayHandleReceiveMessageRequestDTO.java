package com.ItCareerElevatorFifthExercise.DTOs.apiGateway;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApiGatewayHandleReceiveMessageRequestDTO {

    private String sessionId;

    private String content;
}
