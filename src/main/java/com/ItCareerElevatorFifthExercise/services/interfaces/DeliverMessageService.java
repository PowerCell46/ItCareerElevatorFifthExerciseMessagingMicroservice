package com.ItCareerElevatorFifthExercise.services.interfaces;

public interface DeliverMessageService {

    void sendMessageToTheReceiverThroughWebSocket(String serverInstanceAddress, String sessionId, String messageContent);
}
