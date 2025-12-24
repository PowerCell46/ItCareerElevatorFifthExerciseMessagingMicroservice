package com.ItCareerElevatorFifthExercise.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class MessageRequestDTO {

    private String senderId;

    private String senderUsername;

    private String senderEmail;

    private LocationRequestDTO senderLocation;

    private String receiverId;

    private String message;
}
