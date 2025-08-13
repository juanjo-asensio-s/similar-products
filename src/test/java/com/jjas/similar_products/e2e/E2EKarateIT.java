package com.jjas.similar_products.e2e;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.intuit.karate.core.MockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class E2EKarateIT {

    private static MockServer mock;
    private static ConfigurableApplicationContext app;

    @BeforeAll
    static void startAll() {
        mock = MockServer
                .feature("classpath:mocks/mock.feature")
                .http(3001)
                .build();

        final String mockBaseUrl = "http://localhost:3001";

        app = new SpringApplicationBuilder(com.jjas.similar_products.SimilarProductsApplication.class)
                .web(WebApplicationType.SERVLET)
                .profiles("e2e")
                .properties(
                        "server.port=5000",
                        "external.apis.base-url=" + mockBaseUrl
                )
                .run();

        System.out.println(">>> MOCK external @ " + mockBaseUrl);

        int appPort = ((ServletWebServerApplicationContext) app).getWebServer().getPort();
        System.out.println(">>> APP  under test @ http://localhost:" + appPort);

        System.setProperty("app.baseUrl", "http://localhost:" + appPort);
    }

    @AfterAll
    static void stopAll() {
        if (app != null) app.close();
        if (mock != null) mock.stop();
    }

    @Test
        //
    void e2e() {
        Results results = Runner.path("classpath:e2e")
                .parallel(1);
        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

}
