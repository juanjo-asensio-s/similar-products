# Similar Products Service

This is a Spring Boot microservice that implements the backend challenge described in
the [backendDevTest](https://github.com/dalogax/backendDevTest) repository. It exposes a single REST endpoint to
retrieve similar products based on a given product ID. The service is built using Hexagonal Architecture (Ports &
Adapters), applying principles of resilience, caching, and clean code.

## Requirements

- Java 17
- Maven 3.9+
- Docker (optional, for running mocks and load testing)

## Technologies Used

- **Spring Boot 3** Core framework for building the application
- **Spring WebFlux** Used in a limited scope for reactive and non-blocking HTTP client functionality via WebClient.
  Although .block() is currently used for simplicity and clarity, the codebase is prepared to evolve towards full
  reactive pipelines (Mono/Flux) if scalability and performance under high concurrency become a concern.
- **Resilience4j** Circuit Breaker & Retry
- **Spring Cache + Caffeine** Used as a high-performance cache implementation
- **Lombok** For boilerplate code reduction (getters/setters, constructors...)
- **JUnit 5** & **Mockito** Unit testing
- **Wiremock** for Integration testing
- **Karate** E2E testing

## Architecture

This service is structured following Hexagonal (Ports & Adapters) Architecture:

```
com.jjas.similar_products
├─ config                       # Configuration and property classes
├─ domain/                      # Domain model & outbound ports
│  ├─ model/
│  └─ ports/
├─ infrastructure/              # Adapters
│  ├─ rest/                     # REST controller (public API)
│  ├─ http/                     # External API adapter (outbound)
│  │  └─ error/                 # Exceptions + global error handler (Problem+JSON)
│  └─ mapper/                   # MapStruct mappers
└─ generated/                   # OpenAPI generated code
   ├─ similar/                  # Public API DTOs (server)
   └─ external/                 # External API client (client)
```

## Features

- Retrieve product recommendations via `/product/{productId}/similar`
- Integration with external product API (`/product/{id}` and `/product/{id}/similarids`)
- Resilience:
    - Circuit Breakers and Retries with fallback handling
- Caching for both similar IDs and product detail responses
- Interfaces mapped using MapStruct
- Detailed unit testing with JUnit 5 & Mockito
- Integration tests served with Wiremock
- E2E tests implemented using karate

## Public API Contract

Defined in src/main/resources/openapi/similarProducts.yaml. OpenAPI generator produces DTOs and Spring interfaces.

- Endpoint: GET /product/{productId}/similar
- Response: 200 OK -> array of ProductDetail (generated DTO)

### Errors

Errors are returned using application/problem+json:

```json
{
  "type": "about:blank",
  "title": "UPSTREAM_FAILURE",
  "status": 502,
  "detail": "Server error fetching similar IDs",
  "instance": "/product/50/similar",
  "path": "/product/50/similar",
  "timestamp": "2025-08-13T22:59:03.511687+02:00",
  "code": "UPSTREAM_FAILURE"
}

```

## Running the Project

1. **Clone the repository**
   ```bash
   git clone git@github.com:juanjo-asensio-s/similar-products.git
   cd similar-products
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   By default, it runs on port `5000`. This is a requirement to successfully run the tests provided in the original
   project (https://github.com/dalogax/backendDevTest), which expect the Spring Boot application to be listening on this
   port.

*Please note: on macOS, port 5000 is used by the AirPlay Receiver service. Even if you kill the process manually, the
service might restart automatically. To permanently free the port, you must disable AirPlay Receiver via:
System Preferences -> Sharing -> uncheck "AirPlay Receiver".*

## Configuration

Edit the `application.yml` to update the external product API base URL or caching settings:

```yaml
external:
  product-api:
    base-url: http://localhost:3001
spring:
  cache:
    type: simple
```

## Testing

Run all tests using Maven:

```bash
mvn test
```

Run all integration tests using Maven:

```bash
mvn integration-test
```

Run Karate tests:

```bash
mvn -Dtest=E2EKarateIT \
-Dsurefire.useFile=false \
-DtrimStackTrace=false \
-Dkarate.logPrettyRequest=true \
-Dkarate.logPrettyResponse=true \
test
```

## Reference

This implementation is based on the requirements and mock services defined in the following public repository:

[dalogax/backendDevTest](https://github.com/dalogax/backendDevTest)
