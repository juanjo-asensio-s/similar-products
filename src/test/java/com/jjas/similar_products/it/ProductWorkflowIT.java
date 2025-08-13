package com.jjas.similar_products.it;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductWorkflowIT {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @LocalServerPort
    int appPort;

    @Autowired
    TestRestTemplate rest;


    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        r.add("external.product-api.base-url", () -> "http://localhost:" + wm.getPort());
    }

    @BeforeEach
    void clean() {
        wm.resetAll();
    }


    private void stubSimilarIds(String id, String... ids) {
        String json = java.util.Arrays.toString(ids).replaceAll("([a-zA-Z0-9]+)", "\"$1\"");
        wm.stubFor(get(urlPathEqualTo("/product/" + id + "/similarids"))
                .willReturn(okJson(json)));
    }

    private void stubProduct(String id, String name, double price, boolean available) {
        String body = String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"price\":%s,\"availability\":%s}",
                id, name, price, available);
        wm.stubFor(get(urlPathEqualTo("/product/" + id))
                .willReturn(okJson(body)));
    }

    private String appUrl(String path) {
        return "http://localhost:" + appPort + path;
    }


    @Test
    @DisplayName("returns 200 OK with similar products")
    void shouldReturn200WithSimilarProducts() {
        //GIVEN
        stubSimilarIds("1", "2", "3");
        stubProduct("2", "Prod 2", 12.34, true);
        stubProduct("3", "Prod 3", 99.99, false);

        //WHEN
        ResponseEntity<String> resp = rest.getForEntity(appUrl("/product/1/similar"), String.class);

        //THEN
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("\"id\":\"2\"");
        assertThat(resp.getBody()).contains("\"id\":\"3\"");

        wm.verify(getRequestedFor(urlPathEqualTo("/product/1/similarids")));
        wm.verify(getRequestedFor(urlPathEqualTo("/product/2")));
        wm.verify(getRequestedFor(urlPathEqualTo("/product/3")));
    }

    @Test
    @DisplayName("returns 404 Not Found when upstream is 404")
    void shouldReturn404WhenUpstream404() {
        //GIVEN
        wm.stubFor(get(urlPathEqualTo("/product/999/similarids"))
                .willReturn(aResponse().withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"not found\"}")));

        //WHEN
        ResponseEntity<String> resp = rest.getForEntity(appUrl("/product/999/similar"), String.class);

        //THEN
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        wm.verify(getRequestedFor(urlPathEqualTo("/product/999/similarids")));
    }

    @Test
    @DisplayName("returns 502 Bad Gateway when upstream is 5xx")
    void shouldReturn502WhenUpstream5xx() {
        //GIVEN
        wm.stubFor(get(urlPathEqualTo("/product/10/similarids")).willReturn(aResponse().withStatus(502)));

        //WHEN
        ResponseEntity<String> resp = rest.getForEntity(appUrl("/product/10/similar"), String.class);

        //THEN
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        wm.verify(getRequestedFor(urlPathEqualTo("/product/10/similarids")));
    }
}
