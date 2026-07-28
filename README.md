# Stock Management System

[Türkçe README için tıklayın](README.tr.md)

A microservice-based stock/inventory management system built with **Spring Boot** and **Spring Cloud**. The project demonstrates a realistic microservices architecture: centralized configuration, an API gateway, a relational-data service, and a Redis-backed caching service that talks to it over Feign, all orchestrated with Docker Compose and deployable to Kubernetes.

## Architecture

```
                          ┌─────────────────────┐
                          │   Config Server      │
                          │  (Spring Cloud Config)│
                          └──────────┬───────────┘
                                     │ configuration
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
   ┌──────────▼─────────┐ ┌──────────▼─────────┐ ┌──────────▼─────────┐
   │ Spring Cloud Gateway│ │  Product Service    │ │ Product Cache Svc   │
   │   (edge routing)    │─▶  (PostgreSQL, CRUD) │◀─┤  (Redis + Feign)   │
   └──────────────────────┘ └──────────┬──────────┘ └──────────┬─────────┘
                                       │                        │
                              ┌────────▼────────┐      ┌────────▼────────┐
                              │   PostgreSQL     │      │      Redis      │
                              └──────────────────┘      └──────────────────┘
```

## Services

| Service | Description | Default Port | README |
|---|---|---|---|
| [`config-server`](config-server) | Centralized configuration server (Spring Cloud Config), backed by a Git repository | `8888` | [EN](config-server/README.md) / [TR](config-server/README.tr.md) |
| [`spring-cloud-gateway`](spring-cloud-gateway) | API gateway routing external traffic to internal services | `8762` | [EN](spring-cloud-gateway/README.md) / [TR](spring-cloud-gateway/README.tr.md) |
| [`product-service`](product-service) | Core product CRUD service backed by PostgreSQL | `9788` | [EN](product-service/README.md) / [TR](product-service/README.tr.md) |
| [`product-cache-service`](product-cache-service) | Read-through cache in front of `product-service`, backed by Redis | `9791` | [EN](product-cache-service/README.md) / [TR](product-cache-service/README.tr.md) |

Additional folders:
- [`configs`](configs) — Spring Cloud Config profile files (`localhost`, `stage`, `k8s`) served by the config server.
- [`deployment`](deployment) — Docker Compose stack and PostgreSQL init scripts for running everything locally.

## Tech Stack

- **Java 21**
- **Spring Boot** (3.2.x / 3.5.x depending on the service)
- **Spring Cloud** — Config Server, Gateway, OpenFeign
- **PostgreSQL 16** — persistence for `product-service`
- **Redis 7** — caching layer for `product-cache-service`
- **Docker & Docker Compose** — local orchestration
- **Kubernetes manifests** — provided for `product-service` and `spring-cloud-gateway`
- **springdoc-openapi (Swagger UI)** — API documentation
- **Lombok**, **JPA/Hibernate**, **Maven**

## Getting Started

### Prerequisites
- Docker & Docker Compose
- JDK 21 (only needed if you want to build/run services outside Docker)
- A GitHub personal access token, since the config server pulls configuration from a private/public Git repository

### Run with Docker Compose

1. Create a `.env` file next to `deployment/compose.yaml` with the required variables:

```env
GITHUB_USERNAME=your-github-username
GITHUB_TOKEN=your-github-token
DB_NAME=stock_management
DB_USERNAME=postgres
DB_PASSWORD=postgres
REDIS_PASSWORD=your-redis-password
CONFIG_SERVER_URL=http://config-server:8888
```

2. From the `deployment` folder, start the stack:

```bash
cd deployment
docker compose up --build
```

3. Services will become available at:
    - Config Server → `http://localhost:8888`
    - Gateway → `http://localhost:8762`
    - Product Service → `http://localhost:9788`
    - Product Cache Service → `http://localhost:9791`
    - PostgreSQL → `localhost:5433`
    - Redis → `localhost:6379`

The startup order is enforced through Docker Compose health checks: `config-server` and `postgres`/`redis` must be healthy before the dependent services start.

### Run Locally (without Docker)

Each service can also be run individually with its `localhost` Spring profile — see each service's own README for details.

## Configuration Management

All services (except the config server itself) import their configuration from the `config-server` at startup:

```yaml
spring:
  config:
    import: "optional:configserver:http://config-server:8888"
```

The config server, in turn, pulls YAML files from a separate Git repository (`stock-management-configs`) — the [`configs`](configs) folder in this repo mirrors what that Git repository is expected to contain, with profile-specific sections for `localhost`, `stage`, and `k8s`.

## Kubernetes

`product-service` and `spring-cloud-gateway` include Kubernetes manifests (`k8s/`) with Deployment and Service definitions, plus a PostgreSQL Deployment/Service/ConfigMap for `product-service`. These are intended for the `k8s` Spring profile.

## Notes

This project was built as a personal/learning project to practice a microservices architecture (config server, gateway, service discovery patterns via Feign, caching) with Spring Cloud, Docker, and Kubernetes.