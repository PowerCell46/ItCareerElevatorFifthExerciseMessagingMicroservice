package com.ItCareerElevatorFifthExercise.services.interfaces;

import java.time.LocalDateTime;

public interface DeliverMessageService {

    void sendMessageToReceiverThroughWebSocket(
            String serverInstanceAddress, String sessionId,
            String messageContent, LocalDateTime sentAt
    );

    void sendMessageToReceiverThroughEmail(
            String senderId, String receiverId,
            String messageContent, LocalDateTime sentAt
    );
}
