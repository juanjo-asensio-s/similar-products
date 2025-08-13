package com.jjas.similar_products.config;

import com.jjas.similar_products.generated.external.api.DefaultApi;
import com.jjas.similar_products.generated.external.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ExternalApiConfig {

    @Bean
    public ApiClient externalApiClient(
            @Value("${external.product-api.base-url}") String baseUrl,
            @Value("${external.product-api.connect-timeout-ms:2000}") int connectTimeout,
            @Value("${external.product-api.read-timeout-ms:3000}") int readTimeout) {
        SimpleClientHttpRequestFactory reqFactory = new SimpleClientHttpRequestFactory();
        reqFactory.setConnectTimeout(connectTimeout);
        reqFactory.setReadTimeout(readTimeout);

        RestTemplate restTemplate = new RestTemplate(reqFactory);

        ApiClient client = new ApiClient(restTemplate);
        client.setBasePath(baseUrl);
        return client;
    }

    @Bean
    public DefaultApi productsApi(ApiClient client) {
        return new DefaultApi(client);
    }
}
