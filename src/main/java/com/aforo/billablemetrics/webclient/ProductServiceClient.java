package com.aforo.billablemetrics.webclient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

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

    public String getProductTypeById(Long productId) {
    try {
        return productServiceWebClient.get()
                .uri("/{id}", productId)
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .map(ProductResponse::getProductType)
                .block();
    } catch (Exception e) {
        log.warn("Failed to fetch product type: {}", e.getMessage());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot fetch product type for ID: " + productId);
    }
}

@Getter
@Setter
private static class ProductResponse {
    private Long productId;
    private String productName;
    private String productType; // 🔥 ensure backend sends this
}

}
