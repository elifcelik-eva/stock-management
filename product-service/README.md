# Product Service

[Türkçe README için tıklayın](README.tr.md)

Part of the [Stock Management System](../README.md). This is the core service of the system: it owns the `Product` domain and exposes a REST API to create, read, update, and delete products, backed by PostgreSQL.

## Responsibilities

- Full CRUD for products (create, get single, get all, update, soft delete).
- Localized (multi-language) success/error messages — `EN` and `TR` — returned in every response via a `language` path variable.
- Consistent response envelope (`InternalApiResponse`) with a `payload`, an optional friendly `message`, and an `hasError` flag.
- Persists data in PostgreSQL under the `stock_management` schema.
- Fetches its configuration from the `config-server` at startup.
- Exposes Swagger/OpenAPI documentation and an actuator health endpoint (used for Docker/Kubernetes readiness).

## Tech Stack

- Java 21
- Spring Boot 3.2.5
- Spring Web, Spring Data JPA, Spring Cloud Config Client
- PostgreSQL (runtime), H2 (tests)
- springdoc-openapi (Swagger UI)
- Lombok
- JUnit 5 / Spring Boot Test / Spring Security Test

## Data Model

`Product` entity (`stock_management.product` table):

| Field | Type | Notes |
|---|---|---|
| `productId` | `long` | Primary key, auto-generated |
| `productName` | `String` | Required |
| `quantity` | `Integer` | Required |
| `price` | `Double` | Required |
| `productCreatedDate` | `LocalDateTime` | Set automatically on create |
| `productUpdatedDate` | `LocalDateTime` | Set automatically on create/update |
| `deleted` | `boolean` | Soft-delete flag |

## API

Base path: `/api/1.0/product`

All endpoints require a `{language}` path variable — `en` or `tr` — used to localize response messages.

| Method | Path | Description |
|---|---|---|
| `POST` | `/{language}/products` | Create a new product |
| `GET` | `/{language}/products/{productId}` | Get a single product by id |
| `GET` | `/{language}/products` | List all products |
| `PUT` | `/{language}/products/{productId}` | Update an existing product |
| `DELETE` | `/{language}/products/{productId}` | Soft-delete a product |

### Example: create a product

```bash
curl -X POST http://localhost:9788/api/1.0/product/en/products \
  -H "Content-Type: application/json" \
  -d '{
        "productName": "Wireless Mouse",
        "quantity": 100,
        "price": 19.99
      }'
```

### Response envelope shape

```json
{
  "message": {
    "title": "Success",
    "description": "Product successfully created"
  },
  "httpStatus": "CREATED",
  "hasError": false,
  "payload": {
    "productId": 1,
    "productName": "Wireless Mouse",
    "quantity": 100,
    "price": 19.99,
    "productCreatedDate": "2026-07-28T10:00:00",
    "productUpdatedDate": "2026-07-28T10:00:00"
  }
}
```

Custom exceptions (`ProductNotFoundException`, `ProductAlreadyDeletedException`, `ProductNotCreateException`) are handled globally via `GlobalExceptionHandler`, returning localized, friendly error messages.

## Configuration

The service imports its configuration from the config server:

```yaml
spring:
  application:
    name: product-service
  config:
    import: "optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}"
  profiles:
    active: localhost
server:
  port: 9788
```

Datasource settings (username/password/URL, connection pool sizes) are provided per-profile (`localhost`, `stage`, `k8s`) through the config server, using these environment variables:

| Variable | Description |
|---|---|
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `CONFIG_SERVER_URL` | URL of the config server |

## Running

### With Docker Compose (recommended)
See the [root README](../README.md) — this service is started as part of the full stack.

### Standalone (Maven, localhost profile)

```bash
./mvnw spring-boot:run
```

Requires a local PostgreSQL instance reachable at `jdbc:postgresql://localhost:5433/stock_management` (see [`deployment/compose.yaml`](../deployment/compose.yaml) for the matching container setup) and a running config server.

### Tests

```bash
./mvnw test
```

## Port

| Environment | Port |
|---|---|
| Default | `9788` |

## Kubernetes

Manifests are provided under [`k8s/`](k8s):
- `product-service/product-deployment.yaml`, `product-service/product-service.yaml` — Deployment and Service for this application.
- `postgres/postgres-deployment.yaml`, `postgres/postgres-service.yaml`, `postgres/postgres-init-configmap.yaml` — PostgreSQL Deployment, Service, and schema-init ConfigMap.