package com.ItCareerElevatorFifthExercise.services.implementations;

import com.ItCareerElevatorFifthExercise.DTOs.common.ErrorResponseDTO;
import com.ItCareerElevatorFifthExercise.DTOs.request.MessageRequestDTO;
import com.ItCareerElevatorFifthExercise.DTOs.userPresence.MsvcGetUserPresenceResponseDTO;
import com.ItCareerElevatorFifthExercise.exceptions.UserPresenceMicroserviceException;
import com.ItCareerElevatorFifthExercise.services.interfaces.DeliverMessageService;
import com.ItCareerElevatorFifthExercise.services.interfaces.MessageService;
import com.ItCareerElevatorFifthExercise.services.interfaces.PersistMessageService;
import com.ItCareerElevatorFifthExercise.services.interfaces.UserLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
                            requestDTO.getContent()
                    );

        } else {
            deliverMessageService
                    .sendMessageToReceiverThroughEmail(
                            requestDTO.getSenderUsername(),
                            userPresenceResponseDTO.getUserEmail(),
                            requestDTO.getContent()
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
                .block();
    }

    private boolean isReceiverOnline(MsvcGetUserPresenceResponseDTO userPresenceResponseDTO) {
        return
                // @formatter:off
                    userPresenceResponseDTO.getServerInstanceAddress() != null &&
                    userPresenceResponseDTO.getSessionId() != null &&
                    userPresenceResponseDTO.getUserEmail() == null;
                // @formatter:on
    }
}
