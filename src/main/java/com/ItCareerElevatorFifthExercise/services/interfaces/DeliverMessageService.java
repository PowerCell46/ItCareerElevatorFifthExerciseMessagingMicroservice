package com.ItCareerElevatorFifthExercise.services.interfaces;

import com.ItCareerElevatorFifthExercise.DTOs.request.MessageRequestDTO;
import com.ItCareerElevatorFifthExercise.DTOs.userPresence.MsvcGetUserPresenceResponseDTO;

public interface DeliverMessageService {

    void sendMessageToReceiverThroughWebSocketViaMessageBroker(
            MsvcGetUserPresenceResponseDTO userPresenceResponseDTO,
            MessageRequestDTO messageRequestDTO
    );

    void sendMessageToReceiverThroughEmail(MessageRequestDTO messageRequestDTO);
}
