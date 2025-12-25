package com.ItCareerElevatorFifthExercise.DTOs.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class PersistMessageDTO {

    private String senderId;

    private String receiverId;

    private String message;
}
