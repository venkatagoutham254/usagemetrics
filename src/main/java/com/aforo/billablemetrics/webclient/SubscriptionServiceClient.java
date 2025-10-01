package com.aforo.billablemetrics.webclient;

import com.aforo.billablemetrics.tenant.TenantContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceClient {

    private final WebClient subscriptionServiceWebClient;

    private String getBearerToken() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            return "Bearer " + jwt.getTokenValue();
        }
        return null;
    }

    public boolean hasActiveSubscriptionForProduct(Long productId) {
        try {
            SubscriptionItem[] items = subscriptionServiceWebClient.get()
                    .header("Authorization", getBearerToken())
                    .header("X-Organization-Id", String.valueOf(TenantContext.require()))
                    .retrieve()
                    .bodyToMono(SubscriptionItem[].class)
                    .block();
            if (items == null) return false;
            for (SubscriptionItem it : items) {
                if (it == null) continue;
                if (it.getProductId() != null && it.getProductId().equals(productId)) {
                    String st = it.getStatus() == null ? null : it.getStatus().trim().toUpperCase();
                    if ("ACTIVE".equals(st)) return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to check active subscription for product {}: {}", productId, e.getMessage());
            return false;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SubscriptionItem {
        private Long productId;
        private String status;
        public Long getProductId() { return productId; }
        public void setProductId(Long id) { this.productId = id; }
        public String getStatus() { return status; }
        public void setStatus(String s) { this.status = s; }
    }
}
