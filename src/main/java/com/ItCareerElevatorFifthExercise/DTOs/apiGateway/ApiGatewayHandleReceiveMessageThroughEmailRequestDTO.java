package com.ItCareerElevatorFifthExercise.DTOs.apiGateway;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApiGatewayHandleReceiveMessageThroughEmailRequestDTO {

    private String senderId;

    private String receiverId;

    private String content;
}
