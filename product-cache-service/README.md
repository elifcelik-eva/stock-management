# Product Cache Service

[Türkçe README için tıklayın](README.tr.md)

Part of the [Stock Management System](../README.md). This service sits in front of `product-service` and provides a Redis-backed cache for product reads, reducing load on the database-backed service for repeated lookups.

## Responsibilities

- Fetches product data from `product-service` via a **Feign client** (`ProductServiceFeignClient`) when not already cached.
- Caches products in **Redis** and serves subsequent reads from the cache.
- Exposes an endpoint to evict/clear cached products by language.
- Returns the same localized (`EN`/`TR`), consistent response envelope style used across the system.
- Handles the case where `product-service` is unreachable via a dedicated `ProductServiceUnavailableException`.
- Fetches its configuration from the `config-server` at startup, with `fail-fast` and retry enabled.

## Tech Stack

- Java 21
- Spring Boot 3.2.5
- Spring Web, Spring Data Redis
- Spring Cloud OpenFeign, Spring Cloud Config Client
- springdoc-openapi (Swagger UI)
- Lombok

## API

Base path: `/api/1.0/product-cache`

| Method | Path | Description |
|---|---|---|
| `GET` | `/{language}/products/{productId}` | Get a product by id — served from Redis cache, falling back to `product-service` on a cache miss |
| `DELETE` | `/{language}/products` | Evict all cached products |

### Example: get a cached product

```bash
curl http://localhost:9791/api/1.0/product-cache/en/products/1
```

## How It Works

1. A request comes in for a product by id.
2. `ProductService` checks Redis for a cached entry.
3. On a cache miss, it calls `product-service` through the `ProductServiceFeignClient` (registered under the logical name `product-service`, resolved to a real URL per environment via the config server).
4. The result is mapped and stored in Redis for subsequent requests.

```
Client → Product Cache Service → Redis (hit?) 
                              └─(miss)→ product-service (Feign) → Redis (store)
```

## Configuration

```yaml
spring:
  application:
    name: product-cache-service
  config:
    import: "optional:configserver:http://config-server:8888"
  profiles:
    active: localhost
  main:
    allow-bean-definition-overriding: true
info:
  component: Product Cache Service
server:
  port: 9791
```

Redis connection and the Feign target URL for `product-service` are provided per-profile through the config server:

| Variable | Description |
|---|---|
| `REDIS_PASSWORD` | Password for the Redis instance |

| Profile | Redis host | `product-service` Feign URL |
|---|---|---|
| `localhost` | `localhost:6379` | `http://localhost:9788` |
| `stage` | `redis:6379` | `http://product-service:9788` |

## Running

### With Docker Compose (recommended)
See the [root README](../README.md). This service depends on Redis, `product-service`, and `config-server` all being healthy first.

### Standalone (Maven, localhost profile)

```bash
./mvnw spring-boot:run
```

Requires a local Redis instance, a reachable `product-service`, and a running config server.

## Port

| Environment | Port |
|---|---|
| Default | `9791` |