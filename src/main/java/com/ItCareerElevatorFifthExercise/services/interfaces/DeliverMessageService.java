package com.ItCareerElevatorFifthExercise.services.interfaces;

public interface DeliverMessageService {

    void sendMessageToReceiverThroughWebSocket(String serverInstanceAddress, String sessionId, String messageContent);

    void sendMessageToReceiverThroughEmail(String senderId, String messageContent);
}
