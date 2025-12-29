package com.ItCareerElevatorFifthExercise.controllers;

import com.ItCareerElevatorFifthExercise.DTOs.request.MessageRequestDTO;
import com.ItCareerElevatorFifthExercise.services.interfaces.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<String> processMessage(@Valid @RequestBody MessageRequestDTO requestDTO) { // TODO: Change return type
        log.info("---> POST request on api/messages for message sender with id {}.", requestDTO.getSenderId());

        messageService.processMessage(requestDTO);

        return ResponseEntity.created(null).body("Successful message processing."); // TODO: URL
    }
}
