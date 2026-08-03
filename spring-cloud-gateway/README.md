# Spring Cloud Gateway

[Türkçe README için tıklayın](README.tr.md)

Part of the [Stock Management System](../README.md). This is the API gateway of the system: a single entry point that routes external requests to the internal `product-service` and `product-cache-service`.

## Responsibilities

- Routes incoming HTTP requests matching `/api/1.0/product/**` to `product-service`, and requests matching `/api/1.0/product-cache/**` to `product-cache-service`.
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
| `localhost` | `product-cache-service` | `Path=/api/1.0/product-cache/**` | `http://localhost:9791` |
| `stage` | `product-service` | `Path=/api/1.0/product/**` | `http://product-service:9788` |
| `stage` | `product-cache-service` | `Path=/api/1.0/product-cache/**` | `http://product-cache-service:9791` |
| `k8s` | `product-service` | `Path=/api/1.0/product/**` | `http://product-service:9788` |
| `k8s` | `product-cache-service` | `Path=/api/1.0/product-cache/**` | `http://product-cache-service:9791` |

Route `id`s must be unique; Spring Cloud Gateway evaluates routes in order and dispatches to the first one whose predicate matches.

### Example

```bash
# Through the gateway → product-service
curl http://localhost:8762/api/1.0/product/EN/products

# Through the gateway → product-cache-service
curl http://localhost:8762/api/1.0/product-cache/EN/products/1

# Equivalent direct call to product-service
curl http://localhost:9788/api/1.0/product/EN/products
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

Requires a reachable `product-service` (and `product-cache-service` if testing the cache route) and a running config server.

## Port

| Environment | Port |
|---|---|
| Default | `8762` |

## Kubernetes

Manifests are provided under [`k8s/`](k8s):

- `deployment.yaml` — Deployment for this service. Image: `elifcelik49/sm-gateway:latest`. `SPRING_PROFILES_ACTIVE=k8s`.
- `service.yaml` — a `NodePort` Service (port `8762`, `nodePort: 30007`), which is the only entry point exposed outside the cluster. On minikube, get a reachable URL with:

```bash
minikube service spring-cloud-gateway --url
```

```bash
curl http://<minikube-url>/api/1.0/product/EN/products
curl http://<minikube-url>/api/1.0/product-cache/EN/products/1
```