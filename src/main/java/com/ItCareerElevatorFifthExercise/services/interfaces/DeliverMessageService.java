package com.ItCareerElevatorFifthExercise.services.interfaces;

public interface DeliverMessageService {

    void sendMessageToReceiverThroughWebSocket(String serverInstanceAddress, String sessionId, String messageContent);

    void sendMessageToReceiverThroughEmail(String senderUsername, String receiverEmail, String messageContent);
}
