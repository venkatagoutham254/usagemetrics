package com.aforo.billablemetrics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient productServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://54.238.204.246:8080/api/products") // Product client
                .build();
    }

    @Bean
    public WebClient ratePlanServiceWebClient() {
        // Uses the same Product service host/port; rate plan API is in the same service
        return WebClient.builder()
                .baseUrl("http://54.238.204.246:8080/api/rate-plans")
                .build();
    }

    @Bean
    public WebClient ratePlanServiceWebClient() {
        // Uses the same Product service host/port; rate plan API is in the same service
        return WebClient.builder()
                .baseUrl("http://54.238.204.246:8080/api/rate-plans")
                .build();
    }
}
