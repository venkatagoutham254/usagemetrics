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
import com.aforo.billablemetrics.tenant.TenantContext;



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
                    .header("X-Organization-Id", String.valueOf(TenantContext.require()))
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
                    .header("X-Organization-Id", String.valueOf(TenantContext.require()))
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
                    .header("X-Organization-Id", String.valueOf(TenantContext.require()))
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .map(ProductResponse::getProductType)
                    .block();
        } catch (Exception e) {
            log.warn("Failed to fetch product type: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot fetch product type for ID: " + productId);
        }
    }

    public boolean isProductReadyForMetrics(Long productId) {
        try {
            String status = productServiceWebClient.get()
                    .uri("/{id}", productId)
                    .header("Authorization", getBearerToken())   // ✅ forward token
                    .header("X-Organization-Id", String.valueOf(TenantContext.require()))
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .map(ProductResponse::getStatus)
                    .block();
            if (status == null) return false;
            String s = status.trim().toUpperCase();
            // Accept when product is at least configured
            return s.equals("CONFIGURED") || s.equals("MEASURED") || s.equals("PRICED") || s.equals("LIVE");
        } catch (Exception e) {
            log.warn("Failed to fetch product status: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot fetch product status for ID: " + productId);
        }
    }

    // Backward compatibility: treat legacy "active" check as readiness
    public boolean isProductActive(Long productId) {
        return isProductReadyForMetrics(productId);
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
