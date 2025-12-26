package com.ItCareerElevatorFifthExercise.services.implementations;

import com.ItCareerElevatorFifthExercise.DTOs.request.MessageRequestDTO;
import com.ItCareerElevatorFifthExercise.DTOs.kafka.PersistMessageDTO;
import com.ItCareerElevatorFifthExercise.DTOs.kafka.UserLocationDTO;
import com.ItCareerElevatorFifthExercise.services.interfaces.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    @Value("${app.kafka.topics.persist-message}")
    private String PERSIST_MESSAGE_TOPIC_NAME;

    @Value("${app.kafka.topics.user-location}")
    private String USER_LOCATION_TOPIC_NAME;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> persistMessageKafkaTemplate;

    @Override
    public void processMessage(MessageRequestDTO requestDTO) {
        sendKafkaPersistMessage(requestDTO);
        sendKafkaUserLocationMessage(requestDTO);

        // TODO: send the message to the receiver/s
        // TODO: Add some return DTO
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
                    requestDTO.getSenderLocation().getTimestamp()
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
}
