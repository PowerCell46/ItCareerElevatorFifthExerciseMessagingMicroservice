package com.ItCareerElevatorFifthExercise.services.implementations;

import com.ItCareerElevatorFifthExercise.DTOs.apiGateway.ApiGatewayHandleReceiveMessageRequestDTO;
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

    @Value("${api-gateway.internal.deliver-message-path}")
    private String deliverMessagePath;

    @Override
    public void sendMessageToReceiverThroughWebSocket(String serverInstanceAddress, String sessionId, String messageContent) {
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
                                .map(ApiGatewayException::new)
                                .flatMap(Mono::error)
                )
                .toBodilessEntity()
                .retryWhen(buildRetrySpec())
                .block();
    }

    @Override
    public void sendMessageToReceiverThroughEmail(String senderUsername, String receiverEmail, String messageContent) {
        // TODO: send a message to a kafka topic to a new microservice that sends emails to users
        // TODO: don't forget exponential backoff with jitter
        // TODO: Even better, make a request to ApiGateway, to get the email in the db of the receiver and send a kafka message
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
