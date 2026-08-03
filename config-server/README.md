# Config Server

[Türkçe README için tıklayın](README.tr.md)

Part of the [Stock Management System](../README.md). This is a **Spring Cloud Config Server** that serves centralized configuration to all other services (`product-service`, `product-cache-service`, `spring-cloud-gateway`) from a private, remote Git repository.

## Responsibilities

- Exposes configuration files over HTTP so other services can fetch their settings at startup instead of bundling `application.yml` for every environment.
- Backed by a private Git repository (`stock-management-configs`), configured with `clone-on-start` and `force-pull` so it always serves the latest committed configuration.
- Supports three environment profiles: `localhost`, `stage`, and `k8s` — each with its own Git authentication block.

## Tech Stack

- Java 21
- Spring Boot 3.5.11
- Spring Cloud Config Server 2025.0.1

## Configuration

The server's own `application.yaml` points to the Git repository holding the shared configuration files:

```yaml
spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/elifcelik-eva/stock-management-configs.git
          default-label: main
          clone-on-start: true
          force-pull: true
```

For every profile (`localhost`, `stage`, `k8s`), Git authentication credentials are read from environment variables:

| Variable | Description |
|---|---|
| `GITHUB_USERNAME` | Username used to authenticate against the config Git repository |
| `GITHUB_TOKEN` | Personal access token used to authenticate against the config Git repository |

Since the `stock-management-configs` repository is **private**, both variables must be set in every environment, including Kubernetes — there is no anonymous fallback.

## Running

### With Docker

```bash
docker build -t config-server .
docker run -p 8888:8888 \
  -e SPRING_PROFILES_ACTIVE=stage \
  -e GITHUB_USERNAME=your-username \
  -e GITHUB_TOKEN=your-token \
  config-server
```

### With Maven

```bash
./mvnw spring-boot:run
```

### Health Check

The service exposes an actuator health endpoint used by Docker Compose:

```
GET http://localhost:8888/actuator/health
```

## Port

| Environment | Port |
|---|---|
| Default | `8888` |

## Consumers

Every other service in this project imports its configuration from this server at startup, e.g.:

```yaml
spring:
  config:
    import: "optional:configserver:http://config-server:8888"
```

## Kubernetes

Manifests are provided under [`k8s/`](k8s):

- `deployment.yaml` — Deployment for this service. Image: `elifcelik49/sm-config-server:latest`. `SPRING_PROFILES_ACTIVE` is set to `k8s`; `GITHUB_USERNAME`/`GITHUB_TOKEN` are injected from a `github-secret` Secret (`kubectl create secret generic github-secret --from-literal=username=<user> --from-literal=token=<pat>`).
- `service.yaml` — a `ClusterIP` Service named `config-server`, exposing port `8888`. The Service name must match this exactly, since every other service resolves the config server via Kubernetes DNS using the hardcoded/config-imported URL `http://config-server:8888`.

The Git PAT must have at least read access (`repo` scope for classic tokens) to the private `stock-management-configs` repository.