package com.ItCareerElevatorFifthExercise.services.implementations;

import com.ItCareerElevatorFifthExercise.DTOs.kafka.UserLocationDTO;
import com.ItCareerElevatorFifthExercise.DTOs.request.MessageRequestDTO;
import com.ItCareerElevatorFifthExercise.services.interfaces.UserLocationService;
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
public class UserLocationServiceImpl implements UserLocationService {

    @Value("${app.kafka.topics.user-location}")
    private String USER_LOCATION_TOPIC_NAME;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> userLocationKafkaTemplate;

    @Override
    public void sendKafkaUserLocationMessage(MessageRequestDTO requestDTO) {
        try {
            String key = String.format("user-location-%s", requestDTO.getSenderId());
            String value = objectMapper.writeValueAsString(new UserLocationDTO(
                    requestDTO.getSenderId(),
                    requestDTO.getSenderUsername(),
                    requestDTO.getSenderLocation().getLatitude(),
                    requestDTO.getSenderLocation().getLongitude(),
                    requestDTO.getSenderLocation().getRecordedAt()
            ));

            userLocationKafkaTemplate
                    .send(USER_LOCATION_TOPIC_NAME, key, value)
                    .whenComplete((result, ex) -> {
                        if (ex != null) { // TODO: EXPONENTIAL BACKOFF WITH JITTER
                            log.error("Failed to send UserLocationDTO to topic {}.", USER_LOCATION_TOPIC_NAME, ex);

                        } else {
                            log.info("Sent UserLocationDTO {} to topic {} partition {} offset {}.",
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
