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
        long startTime = System.currentTimeMillis();
        log.info("[Product Service] Checking if product exists - productId: {}", productId);
        try {
            productServiceWebClient.get()
                    .uri("/{id}", productId)
                    .header("Authorization", getBearerToken())   // ✅ forward token
                    .retrieve()
                    .bodyToMono(Object.class)
                    .timeout(java.time.Duration.ofSeconds(3))
                    .block();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[Product Service] Product exists check SUCCESS - productId: {}, duration: {}ms", productId, duration);
            return true;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[Product Service] Product validation FAILED - productId: {}, duration: {}ms, error: {}", productId, duration, e.getMessage());
            return false;
        }
    }

    public String getProductNameById(Long productId) {
        long startTime = System.currentTimeMillis();
        log.info("[Product Service] Fetching product name - productId: {}", productId);
        try {
            String productName = productServiceWebClient.get()
                    .uri("/{id}", productId)
                    .header("Authorization", getBearerToken())   // ✅ forward token
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .map(ProductResponse::getProductName)
                    .timeout(java.time.Duration.ofSeconds(3))
                    .block();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[Product Service] Product name fetched SUCCESS - productId: {}, productName: {}, duration: {}ms", productId, productName, duration);
            return productName;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[Product Service] Failed to fetch product name - productId: {}, duration: {}ms, error: {}", productId, duration, e.getMessage());
            return null;
        }
    }

    public String getProductTypeById(Long productId) {
        long startTime = System.currentTimeMillis();
        log.info("[Product Service] Fetching product type - productId: {}", productId);
        try {
            String productType = productServiceWebClient.get()
                    .uri("/{id}", productId)
                    .header("Authorization", getBearerToken())   // ✅ forward token
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .map(ProductResponse::getProductType)
                    .timeout(java.time.Duration.ofSeconds(3))
                    .block();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[Product Service] Product type fetched SUCCESS - productId: {}, productType: {}, duration: {}ms", productId, productType, duration);
            return productType;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Product Service] Failed to fetch product type - productId: {}, duration: {}ms, error: {}", productId, duration, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot fetch product type for ID: " + productId);
        }
    }

    public boolean isProductActive(Long productId) {
        long startTime = System.currentTimeMillis();
        log.info("[Product Service] Checking product active status - productId: {}", productId);
        try {
            String status = productServiceWebClient.get()
                    .uri("/{id}", productId)
                    .header("Authorization", getBearerToken())   // ✅ forward token
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .map(ProductResponse::getStatus)
                    .timeout(java.time.Duration.ofSeconds(3))
                    .block();
            boolean isActive = status != null && "ACTIVE".equalsIgnoreCase(status);
            long duration = System.currentTimeMillis() - startTime;
            log.info("[Product Service] Product status check SUCCESS - productId: {}, status: {}, isActive: {}, duration: {}ms", productId, status, isActive, duration);
            return isActive;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Product Service] Failed to fetch product status - productId: {}, duration: {}ms, error: {}", productId, duration, e.getMessage());
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
