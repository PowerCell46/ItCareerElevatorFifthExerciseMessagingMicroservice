package com.ItCareerElevatorFifthExercise.DTOs.apiGateway;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ApiGatewayHandleReceiveMessageThroughWebSocketDTO {

    private String serverInstanceAddress;

    private String sessionId;

    private String content;

    private String senderId;

    private String senderUsername; // For visualizing data convenience

    private String receiverId;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime sentAt;
}
