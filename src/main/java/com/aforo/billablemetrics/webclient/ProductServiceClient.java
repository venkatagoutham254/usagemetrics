package com.aforo.billablemetrics.webclient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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
                    .header("Authorization", getBearerToken())
                    .header("X-Organization-Id", String.valueOf(TenantContext.require()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            return false; // product does not exist
        } catch (WebClientResponseException e) {
            HttpStatusCode code = e.getStatusCode();
            if (code.value() == 401 || code.value() == 403) {
                throw new ResponseStatusException(code, "Unauthorized to access Product API");
            }
            if (code.is5xxServerError()) {
                // Fallback: try list endpoint and locate product by id
                ProductResponse p = fetchProductViaList(productId);
                if (p != null) return true;
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service error: " + code.value());
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product API error: " + code.value());
        } catch (Exception e) {
            log.warn("Product validation failed due to upstream issue: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service unavailable");
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
        } catch (WebClientResponseException e) {
            HttpStatusCode code = e.getStatusCode();
            if (code.is5xxServerError()) {
                ProductResponse p = fetchProductViaList(productId);
                return p == null ? null : p.getProductName();
            }
            log.warn("Failed to fetch product name ({}): {}", code.value(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Failed to fetch product name: {}", e.getMessage());
            return null;
        }
    }

    public String getProductTypeById(Long productId) {
        try {
            return productServiceWebClient.get()
                    .uri("/{id}", productId)
                    .header("Authorization", getBearerToken())
                    .header("X-Organization-Id", String.valueOf(TenantContext.require()))
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .map(ProductResponse::getProductType)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid productId: " + productId);
        } catch (WebClientResponseException e) {
            HttpStatusCode code = e.getStatusCode();
            if (code.value() == 401 || code.value() == 403) {
                throw new ResponseStatusException(code, "Unauthorized to access Product API");
            }
            if (code.is5xxServerError()) {
                ProductResponse p = fetchProductViaList(productId);
                if (p != null && p.getProductType() != null) return p.getProductType();
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service error: " + code.value());
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product API error: " + code.value());
        } catch (Exception e) {
            log.warn("Failed to fetch product type: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service unavailable");
        }
    }

    public boolean isProductReadyForMetrics(Long productId) {
        try {
            String status = productServiceWebClient.get()
                    .uri("/{id}", productId)
                    .header("Authorization", getBearerToken())
                    .header("X-Organization-Id", String.valueOf(TenantContext.require()))
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .map(ProductResponse::getStatus)
                    .block();
            if (status == null) return false;
            String s = status.trim().toUpperCase();
            return s.equals("CONFIGURED") || s.equals("MEASURED") || s.equals("PRICED") || s.equals("LIVE");
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid productId: " + productId);
        } catch (WebClientResponseException e) {
            HttpStatusCode code = e.getStatusCode();
            if (code.value() == 401 || code.value() == 403) {
                throw new ResponseStatusException(code, "Unauthorized to access Product API");
            }
            if (code.is5xxServerError()) {
                ProductResponse p = fetchProductViaList(productId);
                if (p != null && p.getStatus() != null) {
                    String s = p.getStatus().trim().toUpperCase();
                    return s.equals("CONFIGURED") || s.equals("MEASURED") || s.equals("PRICED") || s.equals("LIVE");
                }
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service error: " + code.value());
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product API error: " + code.value());
        } catch (Exception e) {
            log.warn("Failed to fetch product status: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service unavailable");
        }
    }

    private ProductResponse fetchProductViaList(Long productId) {
        try {
            ProductResponse[] list = productServiceWebClient.get()
                    .uri("")
                    .header("Authorization", getBearerToken())
                    .header("X-Organization-Id", String.valueOf(TenantContext.require()))
                    .retrieve()
                    .bodyToMono(ProductResponse[].class)
                    .block();
            if (list == null) return null;
            for (ProductResponse p : list) {
                if (p != null && p.getProductId() != null && p.getProductId().equals(productId)) {
                    return p;
                }
            }
            return null;
        } catch (Exception ex) {
            log.warn("Fallback list fetch failed: {}", ex.getMessage());
            return null;
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
