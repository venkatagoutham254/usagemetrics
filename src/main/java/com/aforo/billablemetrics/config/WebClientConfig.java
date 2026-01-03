package com.aforo.billablemetrics.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    // CRITICAL: Timeouts prevent connection pool exhaustion from circular dependency
    private HttpClient createHttpClientWithTimeouts() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .responseTimeout(Duration.ofSeconds(5))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS)));
    }

    @Bean
    public WebClient productServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://3.208.93.68:8080/api/products")
                .clientConnector(new ReactorClientHttpConnector(createHttpClientWithTimeouts()))
                .build();
    }

    @Bean
    public WebClient ratePlanServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://3.208.93.68:8080/api/rate-plans")
                .clientConnector(new ReactorClientHttpConnector(createHttpClientWithTimeouts()))
                .build();
    }

    @Bean
    public WebClient customerServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://44.201.19.187:8081")
                .clientConnector(new ReactorClientHttpConnector(createHttpClientWithTimeouts()))
                .build();
    }
}