package com.ItCareerElevatorFifthExercise.services.implementations;

import com.ItCareerElevatorFifthExercise.DTOs.common.ErrorResponseDTO;
import com.ItCareerElevatorFifthExercise.DTOs.request.MessageRequestDTO;
import com.ItCareerElevatorFifthExercise.DTOs.userPresence.MsvcGetUserPresenceResponseDTO;
import com.ItCareerElevatorFifthExercise.exceptions.UserPresenceMicroserviceException;
import com.ItCareerElevatorFifthExercise.services.interfaces.DeliverMessageService;
import com.ItCareerElevatorFifthExercise.services.interfaces.MessageService;
import com.ItCareerElevatorFifthExercise.services.interfaces.PersistMessageService;
import com.ItCareerElevatorFifthExercise.services.interfaces.UserLocationService;
import com.ItCareerElevatorFifthExercise.util.RetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final WebClient userPresenceWebClient;

    private final UserLocationService userLocationService;
    private final DeliverMessageService deliverMessageService;
    private final PersistMessageService persistMessageService;

    @Override
    public void processMessage(MessageRequestDTO requestDTO) {
        persistMessageService.sendKafkaPersistMessage(requestDTO);
        userLocationService.sendKafkaUserLocationMessage(requestDTO);

        var userPresenceResponseDTO = getReceiverPresence(requestDTO.getReceiverId());

        if (isReceiverOnline(userPresenceResponseDTO)) {
            deliverMessageService
                    .sendMessageToReceiverThroughWebSocket(
                            userPresenceResponseDTO.getServerInstanceAddress(),
                            userPresenceResponseDTO.getSessionId(),
                            requestDTO.getContent(),
                            requestDTO.getSentAt()
                    );

        } else {
            deliverMessageService
                    .sendMessageToReceiverThroughEmail(
                            requestDTO.getSenderId(),
                            requestDTO.getReceiverId(),
                            requestDTO.getContent(),
                            requestDTO.getSentAt()
                    );
        }
    }

    private MsvcGetUserPresenceResponseDTO getReceiverPresence(String receiverId) {
        return userPresenceWebClient
                .get()
                .uri("/api/userPresence/" + receiverId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, // TODO: Look for a better approach (test all possible custom errors)
                        resp -> resp
                                .bodyToMono(ErrorResponseDTO.class)
                                .map(UserPresenceMicroserviceException::new)
                                .flatMap(Mono::error)
                )
                .bodyToMono(MsvcGetUserPresenceResponseDTO.class)
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

                    return new UserPresenceMicroserviceException(error);
                });
    }

    private boolean isReceiverOnline(MsvcGetUserPresenceResponseDTO userPresenceResponseDTO) {
        return
                // @formatter:off
                    userPresenceResponseDTO.getServerInstanceAddress() != null ||
                    userPresenceResponseDTO.getSessionId() != null;
                // @formatter:on
    }
}
