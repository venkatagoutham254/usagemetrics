package com.aforo.billablemetrics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient productServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://product.dev.aforo.space:8080/api/products")
                .build();
    }

    @Bean
    public WebClient ratePlanServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://product.dev.aforo.space:8080/api/rate-plans")
                .build();
    }

    @Bean
    public WebClient customerServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://org.dev.aforo.space:8081")
                .build();
    }
}