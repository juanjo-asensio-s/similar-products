package com.jjas.similar_products.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient productApiWebClient(ProductApiProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
