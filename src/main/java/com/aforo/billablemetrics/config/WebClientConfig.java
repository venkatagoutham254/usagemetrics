package com.aforo.billablemetrics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient productServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://3.208.93.68:8080/api/products")
                .build();
    }

    @Bean
    public WebClient ratePlanServiceWebClient() {
        // Uses the same Product service host/port; rate plan API is in the same service
        return WebClient.builder()
                .baseUrl("http://3.208.93.68:8080/api/rate-plans")
                .build();
    }

    @Bean
    public WebClient customerServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://44.201.19.187:8081")
                .build();
    }
}