package com.aforo.billablemetrics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient productServiceWebClient(
            WebClient.Builder builder,
            @Value("${product.service.url:http://54.238.204.246:8080}") String baseUrl) {
        return builder
                .baseUrl(baseUrl + "/api/products")
                .build();
    }

    @Bean
    public WebClient ratePlanServiceWebClient(
            WebClient.Builder builder,
            @Value("${product.service.url:http://54.238.204.246:8080}") String baseUrl) {
        // Uses the same Product service host/port; rate plan API is in the same service
        return builder
                .baseUrl(baseUrl + "/api/rateplans")
                .build();
    }

    @Bean
    public WebClient subscriptionServiceWebClient(
            WebClient.Builder builder,
            @Value("${subscriptions.service.url:http://13.113.70.183:8084}") String baseUrl) {
        return builder
                .baseUrl(baseUrl + "/api/subscriptions")
                .build();
    }
}
