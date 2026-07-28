# Spring Cloud Gateway

[Türkçe README için tıklayın](README.tr.md)

Part of the [Stock Management System](../README.md). This is the API gateway of the system: a single entry point that routes external requests to the internal `product-service`.

## Responsibilities

- Routes incoming HTTP requests matching `/api/1.0/product/**` to `product-service`.
- Centralizes the edge of the system so clients only need to know one address.
- Fetches its routing configuration from the `config-server` at startup, meaning routes can be changed per environment without redeploying the gateway.
- Exposes an actuator health endpoint used by Docker/Kubernetes.

## Tech Stack

- Java 21
- Spring Boot 3.2.5
- Spring Cloud Gateway
- Spring Cloud Config Client
- Spring Boot Actuator

## Routing

Routes are defined per Spring profile in the shared configuration (served by `config-server`):

| Profile | Route id | Predicate | Target URI |
|---|---|---|---|
| `localhost` | `product-service` | `Path=/api/1.0/product/**` | `http://localhost:9788` |
| `stage` | `product-service` | `Path=/api/1.0/product/**` | `http://product-service:9788` |
| `k8s` | `product-service` | `Path=/api/1.0/product/**` | `http://product-service:9788` |

### Example

```bash
# Through the gateway
curl http://localhost:8762/api/1.0/product/en/products

# Equivalent direct call to product-service
curl http://localhost:9788/api/1.0/product/en/products
```

## Configuration

```yaml
spring:
  application:
    name: spring-cloud-gateway
  config:
    import: "optional:configserver:http://config-server:8888"
  profiles:
    active: localhost
server:
  port: 8762
```

## Running

### With Docker Compose (recommended)
See the [root README](../README.md). This service starts after `product-service` and `config-server` are healthy.

### Standalone (Maven, localhost profile)

```bash
./mvnw spring-boot:run
```

Requires a reachable `product-service` and a running config server.

## Port

| Environment | Port |
|---|---|
| Default | `8762` |

## Kubernetes

Manifests are provided under [`k8s/`](k8s) with a Deployment and Service for this application, intended for the `k8s` Spring profile.