package com.ItCareerElevatorFifthExercise.services.implementations;

import com.ItCareerElevatorFifthExercise.DTOs.apiGateway.ApiGatewayHandleReceiveMessageThroughEmailRequestDTO;
import com.ItCareerElevatorFifthExercise.DTOs.apiGateway.ApiGatewayHandleReceiveMessageThroughWebSocketDTO;
import com.ItCareerElevatorFifthExercise.DTOs.common.ErrorResponseDTO;
import com.ItCareerElevatorFifthExercise.DTOs.request.MessageRequestDTO;
import com.ItCareerElevatorFifthExercise.DTOs.userPresence.MsvcGetUserPresenceResponseDTO;
import com.ItCareerElevatorFifthExercise.exceptions.ApiGatewayException;
import com.ItCareerElevatorFifthExercise.services.interfaces.DeliverMessageService;
import com.ItCareerElevatorFifthExercise.util.RetryPolicy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliverMessageServiceImpl implements DeliverMessageService {

    @Value("${app.kafka.topics.forward-message}")
    private String FORWARD_MESSAGE_TOPIC_NAME;

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, String> forwardMessageKafkaTemplate;

    @Value("${api-gateway.base-endpoint}")
    private String API_GATEWAY_BASE_URL;

    @Value("${api-gateway.internal.deliver-message-through-email-path}")
    private String deliverMessageThroughEmailPath;

    @Override
    public void sendMessageToReceiverThroughWebSocketViaMessageBroker(
            MsvcGetUserPresenceResponseDTO userPresenceResponseDTO,
            MessageRequestDTO messageRequestDTO
    ) {
        try {
            String key = String.format("forward-message-%s", userPresenceResponseDTO.getSessionId());

            String value = objectMapper
                    .writeValueAsString(new ApiGatewayHandleReceiveMessageThroughWebSocketDTO(
                            userPresenceResponseDTO.getServerInstanceAddress(),
                            userPresenceResponseDTO.getSessionId(),
                            messageRequestDTO.getContent(),
                            messageRequestDTO.getSenderId(),
                            messageRequestDTO.getSenderUsername(),
                            messageRequestDTO.getSentAt()
                    ));

            forwardMessageKafkaTemplate
                    .send(FORWARD_MESSAGE_TOPIC_NAME, key, value)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send ApiGatewayHandleReceiveMessageThroughWebSocketDTO to topic {}.", FORWARD_MESSAGE_TOPIC_NAME, ex);

                        } else {
                            log.info("Sent ApiGatewayHandleReceiveMessageThroughWebSocketDTO {} to topic {} partition {} offset {}.",
                                    key,
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()
                            );
                        }
                    });

        } catch (JsonProcessingException ex) { // TODO: Retry
            log.error("Failed to serialize ApiGatewayHandleReceiveMessageThroughWebSocketDTO to JSON.", ex);
        }
    }

    @Override
    public void sendMessageToReceiverThroughEmail(MessageRequestDTO messageRequestDTO) {
        var requestDTO = new ApiGatewayHandleReceiveMessageThroughEmailRequestDTO(
                messageRequestDTO.getSenderId(),
                messageRequestDTO.getSenderUsername(),
                messageRequestDTO.getReceiverId(),
                messageRequestDTO.getContent(),
                messageRequestDTO.getSentAt()
        );

        webClient.post()
                .uri(API_GATEWAY_BASE_URL + deliverMessageThroughEmailPath)
                .bodyValue(requestDTO)
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
