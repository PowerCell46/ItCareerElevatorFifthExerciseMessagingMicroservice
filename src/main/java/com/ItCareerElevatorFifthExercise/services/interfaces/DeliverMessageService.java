package com.ItCareerElevatorFifthExercise.services.interfaces;

import com.ItCareerElevatorFifthExercise.DTOs.request.MessageRequestDTO;
import com.ItCareerElevatorFifthExercise.DTOs.userPresence.MsvcGetUserPresenceResponseDTO;

import java.time.LocalDateTime;

public interface DeliverMessageService {

    void sendMessageToReceiverThroughWebSocketViaMessageBroker(
            MsvcGetUserPresenceResponseDTO userPresenceResponseDTO,
            MessageRequestDTO messageRequestDTO
    );

    void sendMessageToReceiverThroughEmail(
            String senderId, String receiverId,
            String messageContent, LocalDateTime sentAt
    );
}
