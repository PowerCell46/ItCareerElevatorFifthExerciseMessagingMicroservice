package com.ItCareerElevatorFifthExercise.services.implementations;

import com.ItCareerElevatorFifthExercise.DTOs.apiGateway.ApiGatewayHandleReceiveMessageRequestDTO;
import com.ItCareerElevatorFifthExercise.DTOs.common.ErrorResponseDTO;
import com.ItCareerElevatorFifthExercise.DTOs.userPresence.MsvcGetUserPresenceResponseDTO;
import com.ItCareerElevatorFifthExercise.exceptions.UserPresenceMicroserviceException;
import com.ItCareerElevatorFifthExercise.services.interfaces.DeliverMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliverMessageServiceImpl implements DeliverMessageService {

    private final WebClient webClient;

    @Value("${api-gateway.internal.deliver-message-path}")
    private String deliverMessagePath;

    @Override
    public void sendMessageToTheReceiverThroughWebSocket(String serverInstanceAddress, String sessionId, String messageContent) {
        var receiveMessageRequestDTO = new ApiGatewayHandleReceiveMessageRequestDTO(
                sessionId,
                messageContent
        );

        String url = String.format("http://%s%s", serverInstanceAddress, deliverMessagePath); // TODO: HTTP?

        webClient.post()
                .uri(url)
                .bodyValue(receiveMessageRequestDTO)
                .retrieve()
                .onStatus(HttpStatusCode::isError, // TODO: Look for a better approach (test all possible custom errors)
                        resp -> resp
                                .bodyToMono(ErrorResponseDTO.class)
                                .map(UserPresenceMicroserviceException::new)
                                .flatMap(Mono::error)
                )
                .toBodilessEntity()
                .block();
    }
}
