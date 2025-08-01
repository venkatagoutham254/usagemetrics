package com.aforo.billablemetrics.webclient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final WebClient productServiceWebClient;

    public boolean productExists(Long productId) {
        try {
            productServiceWebClient.get()
                    .uri("/{id}", productId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("Product validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getProductNameById(Long productId) {
        try {
            return productServiceWebClient.get()
                    .uri("/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .map(ProductResponse::getProductName)
                    .block();
        } catch (Exception e) {
            log.warn("Failed to fetch product name: {}", e.getMessage());
            return null;
        }
    }

    @Getter
    @Setter
    private static class ProductResponse {
        private Long productId;
        private String productName;
    }
}
