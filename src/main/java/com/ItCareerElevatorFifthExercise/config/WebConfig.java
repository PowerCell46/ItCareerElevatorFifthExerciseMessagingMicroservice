package com.ItCareerElevatorFifthExercise.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {

    @Value("${user-presence-microservice.base-endpoint}")
    private String USER_PRESENCE_MICROSERVICE_BASE_URL;

    @Bean
    public WebClient userPresenceWebClient() {
        return WebClient
                .builder()
                .baseUrl(USER_PRESENCE_MICROSERVICE_BASE_URL)
                .build();
    }
}
