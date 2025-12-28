package com.ItCareerElevatorFifthExercise.services.interfaces;

import com.ItCareerElevatorFifthExercise.DTOs.request.MessageRequestDTO;

public interface UserLocationService {

    void sendKafkaUserLocationMessage(MessageRequestDTO requestDTO);
}
