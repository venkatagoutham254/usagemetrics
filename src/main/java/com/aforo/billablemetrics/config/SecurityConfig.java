package com.aforo.billablemetrics.config;

import com.aforo.billablemetrics.security.JwtTenantFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${aforo.jwt.secret}")
    private String jwtSecret; // injected from application.yml or env

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtTenantFilter jwtTenantFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Allow Swagger & health endpoints without auth
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api/health").permitAll()
                // All billable-metrics APIs need JWT
                .requestMatchers(HttpMethod.POST, "/api/billable-metrics/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/billable-metrics/**").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/billable-metrics/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/billable-metrics/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/billable-metrics/**").authenticated()
                // everything else
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()))
            );

        // ✅ Ensure the tenant filter runs AFTER Bearer token authentication
        http.addFilterAfter(jwtTenantFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256")
        ).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
