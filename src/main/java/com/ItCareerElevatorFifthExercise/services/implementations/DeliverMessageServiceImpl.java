package com.ItCareerElevatorFifthExercise.services.implementations;

import com.ItCareerElevatorFifthExercise.DTOs.apiGateway.ApiGatewayHandleReceiveMessageThroughEmailRequestDTO;
import com.ItCareerElevatorFifthExercise.DTOs.apiGateway.ApiGatewayHandleReceiveMessageThroughWebSocketRequestDTO;
import com.ItCareerElevatorFifthExercise.DTOs.common.ErrorResponseDTO;
import com.ItCareerElevatorFifthExercise.exceptions.ApiGatewayException;
import com.ItCareerElevatorFifthExercise.services.interfaces.DeliverMessageService;
import com.ItCareerElevatorFifthExercise.util.RetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliverMessageServiceImpl implements DeliverMessageService {

    private final WebClient webClient;

    @Value("${api-gateway.base-endpoint}")
    private String API_GATEWAY_BASE_URL;

    @Value("${api-gateway.internal.deliver-message-through-web-socket-path}")
    private String deliverMessageThroughWebSocketPath;

    @Value("${api-gateway.internal.deliver-message-through-email-path}")
    private String deliverMessageThroughEmailPath;

    @Override
    public void sendMessageToReceiverThroughWebSocket(String serverInstanceAddress, String sessionId, String messageContent) {
        var receiveMessageRequestDTO = new ApiGatewayHandleReceiveMessageThroughWebSocketRequestDTO(
                sessionId,
                messageContent
        );

        String url = String.format("http://%s%s", serverInstanceAddress, deliverMessageThroughWebSocketPath); // TODO: HTTP?

        webClient.post()
                .uri(url)
                .bodyValue(receiveMessageRequestDTO)
                .retrieve()
                .onStatus(HttpStatusCode::isError, // TODO: Look for a better approach (test all possible custom errors)
                        resp -> resp
                                .bodyToMono(ErrorResponseDTO.class)
                                .map(ApiGatewayException::new)
                                .flatMap(Mono::error)
                )
                .toBodilessEntity()
                .retryWhen(buildRetrySpec())
                .block();
    }

    @Override
    public void sendMessageToReceiverThroughEmail(String senderId, String receiverId, String messageContent) {
        var receiveMessageRequestDTO = new ApiGatewayHandleReceiveMessageThroughEmailRequestDTO(
                senderId,
                receiverId,
                messageContent
        );

        webClient.post()
                .uri(API_GATEWAY_BASE_URL + deliverMessageThroughEmailPath)
                .bodyValue(receiveMessageRequestDTO)
                .retrieve()
                .onStatus(HttpStatusCode::isError, // TODO: Look for a better approach (test all possible custom errors)
                        resp -> resp
                                .bodyToMono(ErrorResponseDTO.class)
                                .map(ApiGatewayException::new)
                                .flatMap(Mono::error)
                )
                .toBodilessEntity()
                .retryWhen(buildRetrySpec())
                .block();
    }

    private Retry buildRetrySpec() {
        return Retry
                .backoff(4, Duration.ofSeconds(2)) // 2s, 4s, 8s, 16s
                .maxBackoff(Duration.ofSeconds(20))
                .jitter(0.5d) // 50% jitter
                .filter(RetryPolicy::isRetriable)
                .onRetryExhaustedThrow((spec, signal) -> {
                    Throwable failure = signal.failure();

                    ErrorResponseDTO error = new ErrorResponseDTO(
                            500,
                            failure.getMessage() != null ? failure.getMessage() : "Internal server error occurred.",
                            System.currentTimeMillis()
                    );

                    return new ApiGatewayException(error);
                });
    }
}
