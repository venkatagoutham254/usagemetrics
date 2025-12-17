package com.aforo.billablemetrics.webclient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;



@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final WebClient productServiceWebClient;

    private String getBearerToken() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            return "Bearer " + jwt.getTokenValue();
        }
        return null;
    }

    public boolean productExists(Long productId) {
        try {
            productServiceWebClient.get()
                    .uri("/{id}", productId)
                    .header("Authorization", getBearerToken())   // ✅ forward token
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
                    .header("Authorization", getBearerToken())   // ✅ forward token
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
                    .header("Authorization", getBearerToken())   // ✅ forward token
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .map(ProductResponse::getProductType)
                    .block();
        } catch (Exception e) {
            log.warn("Failed to fetch product type: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot fetch product type for ID: " + productId);
        }
    }

    public boolean isProductActive(Long productId) {
        try {
            String status = productServiceWebClient.get()
                    .uri("/{id}", productId)
                    .header("Authorization", getBearerToken())   // ✅ forward token
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .map(ProductResponse::getStatus)
                    .block();
            return status != null && "ACTIVE".equalsIgnoreCase(status);
        } catch (Exception e) {
            log.warn("Failed to fetch product status: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot fetch product status for ID: " + productId);
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ProductResponse {
        private Long productId;
        private String productName;
        private String productType; // 🔥 ensure backend sends this
        private String status; // product status from Product API
    }
}
