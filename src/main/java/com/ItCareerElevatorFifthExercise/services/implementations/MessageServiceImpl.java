package com.ItCareerElevatorFifthExercise.services.implementations;

import com.ItCareerElevatorFifthExercise.DTOs.apiGateway.ReceiveMessageRequestDTO;
import com.ItCareerElevatorFifthExercise.DTOs.common.ErrorResponseDTO;
import com.ItCareerElevatorFifthExercise.DTOs.request.MessageRequestDTO;
import com.ItCareerElevatorFifthExercise.DTOs.kafka.PersistMessageDTO;
import com.ItCareerElevatorFifthExercise.DTOs.kafka.UserLocationDTO;
import com.ItCareerElevatorFifthExercise.DTOs.userPresence.MsvcGetUserPresenceResponseDTO;
import com.ItCareerElevatorFifthExercise.exceptions.UserPresenceMicroserviceException;
import com.ItCareerElevatorFifthExercise.services.interfaces.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    @Value("${app.kafka.topics.persist-message}")
    private String PERSIST_MESSAGE_TOPIC_NAME;

    @Value("${app.kafka.topics.user-location}")
    private String USER_LOCATION_TOPIC_NAME;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final WebClient userPresenceWebClient;
    private final KafkaTemplate<String, String> persistMessageKafkaTemplate;

    @Override
    public void processMessage(MessageRequestDTO requestDTO) {
        sendKafkaPersistMessage(requestDTO);
        sendKafkaUserLocationMessage(requestDTO);

        // TODO: Also handle groups...

        MsvcGetUserPresenceResponseDTO userPresenceResponseDTO = getUserPresence(requestDTO.getReceiverId());
        if (userPresenceResponseDTO.getServerInstanceAddress() != null) {
            sendMessageToTheReceiverThroughWebSocket(userPresenceResponseDTO, requestDTO.getContent());
        } else {
//            TODO: send through mail
        }
    }

    private void sendKafkaPersistMessage(MessageRequestDTO requestDTO) {
        try {
            String key = String.format("user-message-%s", requestDTO.getSenderId());
            String value = objectMapper.writeValueAsString(new PersistMessageDTO(
                    requestDTO.getSenderId(),
                    requestDTO.getReceiverId(),
                    requestDTO.getContent(),
                    requestDTO.getSentAt()
            ));

            persistMessageKafkaTemplate
                    .send(PERSIST_MESSAGE_TOPIC_NAME, key, value)
                    .whenComplete((result, ex) -> {
                        if (ex != null) { // TODO: EXPONENTIAL BACKOFF WITH JITTER
                            log.error("Failed to send MessageRequestDTO to topic {}.", PERSIST_MESSAGE_TOPIC_NAME, ex);

                        } else {
                            log.info("Sent persistMessageDTO {} to topic {} partition {} offset {}.",
                                    key,
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()
                            );
                        }
                    });

        } catch (JsonProcessingException ex) { // TODO: Retry
            log.error("Failed to serialize MessageRequestDTO to JSON", ex);
        }
    }

    private void sendKafkaUserLocationMessage(MessageRequestDTO requestDTO) {
        try {
            String key = String.format("user-location-%s", requestDTO.getSenderId());
            String value = objectMapper.writeValueAsString(new UserLocationDTO(
                    requestDTO.getSenderId(),
                    requestDTO.getSenderUsername(),
                    requestDTO.getSenderLocation().getLatitude(),
                    requestDTO.getSenderLocation().getLongitude(),
                    requestDTO.getSenderLocation().getRecordedAt()
            ));

            persistMessageKafkaTemplate
                    .send(USER_LOCATION_TOPIC_NAME, key, value)
                    .whenComplete((result, ex) -> {
                        if (ex != null) { // TODO: EXPONENTIAL BACKOFF WITH JITTER
                            log.error("Failed to send MessageRequestDTO to topic {}.", PERSIST_MESSAGE_TOPIC_NAME, ex);

                        } else {
                            log.info("Sent userLocationDTO {} to topic {} partition {} offset {}.",
                                    key,
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()
                            );
                        }
                    });

        } catch (JsonProcessingException ex) { // TODO: Retry
            log.error("Failed to serialize MessageRequestDTO to JSON", ex);
        }
    }

    private MsvcGetUserPresenceResponseDTO getUserPresence(String receiverId) {
        return userPresenceWebClient
                .get()
                .uri("/api/userPresence/" + receiverId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, // TODO: Look for a better approach (test all possible custom errors)
                        resp -> resp // TODO: Handle or retry
                                .bodyToMono(ErrorResponseDTO.class)
                                .map(UserPresenceMicroserviceException::new)
                                .flatMap(Mono::error)
                )
                .bodyToMono(MsvcGetUserPresenceResponseDTO.class)
                .block();
    }

    private void sendMessageToTheReceiverThroughWebSocket(MsvcGetUserPresenceResponseDTO responseDTO, String messageContent) {
        ReceiveMessageRequestDTO receiveMessageRequestDTO = new ReceiveMessageRequestDTO(
                responseDTO.getSessionId(),
                messageContent
        );

        String url = String.format("http://%s/internal/deliverMessage", responseDTO.getServerInstanceAddress());

        webClient.post()
                .uri(url)
                .bodyValue(receiveMessageRequestDTO)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> resp
                                .bodyToMono(ErrorResponseDTO.class)
                                .map(UserPresenceMicroserviceException::new)
                                .flatMap(Mono::error)
                )
                .toBodilessEntity()
                .block();
    }
}
