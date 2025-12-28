package com.ItCareerElevatorFifthExercise.services.interfaces;

import com.ItCareerElevatorFifthExercise.DTOs.request.MessageRequestDTO;

public interface PersistMessageService {

    void sendKafkaPersistMessage(MessageRequestDTO requestDTO);
}
