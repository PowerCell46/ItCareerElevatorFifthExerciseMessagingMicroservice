package com.ItCareerElevatorFifthExercise.services.implementations;

import com.ItCareerElevatorFifthExercise.DTOs.kafka.PersistMessageDTO;
import com.ItCareerElevatorFifthExercise.DTOs.request.MessageRequestDTO;
import com.ItCareerElevatorFifthExercise.services.interfaces.PersistMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistMessageServiceImpl implements PersistMessageService {

    @Value("${app.kafka.topics.persist-message}")
    private String PERSIST_MESSAGE_TOPIC_NAME;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> persistMessageKafkaTemplate;

    @Override
    public void sendKafkaPersistMessage(MessageRequestDTO requestDTO) {
        try {
            String key = String.format("persist-message-user-%s", requestDTO.getSenderId());
            String value = objectMapper.writeValueAsString(new PersistMessageDTO(
                    requestDTO.getSenderId(),
                    requestDTO.getReceiverId(),
                    requestDTO.getContent(),
                    requestDTO.getSentAt()
            ));

            persistMessageKafkaTemplate
                    .send(PERSIST_MESSAGE_TOPIC_NAME, key, value)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send PersistMessageDTO to topic {}.", PERSIST_MESSAGE_TOPIC_NAME, ex);

                        } else {
                            log.info("Sent PersistMessageDTO {} to topic {} partition {} offset {}.",
                                    key,
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()
                            );
                        }
                    });

        } catch (JsonProcessingException ex) { // TODO: Retry
            log.error("Failed to serialize PersistMessageDTO to JSON.", ex);
        }
    }
}
