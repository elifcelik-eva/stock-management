# Config Server

[For English README click here](README.md)

[Stock Management System](../README.md) projesinin bir parçasıdır. Bu, diğer tüm servislere (`product-service`, `product-cache-service`, `spring-cloud-gateway`) private, uzak bir Git deposu üzerinden merkezi konfigürasyon sunan bir **Spring Cloud Config Server**'dır.

## Sorumluluklar

- Her ortam için ayrı `application.yml` paketlemek yerine, konfigürasyon dosyalarını HTTP üzerinden sunar; diğer servisler ayarlarını açılışta buradan çeker.
- Private bir Git deposu (`stock-management-configs`) tarafından beslenir; `clone-on-start` ve `force-pull` ayarları sayesinde her zaman en güncel commit edilmiş konfigürasyonu sunar.
- Üç ortam profilini destekler: `localhost`, `stage` ve `k8s` — her biri kendi Git kimlik doğrulama bloğuna sahip.

## Kullanılan Teknolojiler

- Java 21
- Spring Boot 3.5.11
- Spring Cloud Config Server 2025.0.1

## Konfigürasyon

Servisin kendi `application.yaml`'ı, paylaşılan konfigürasyon dosyalarını barındıran Git deposunu işaret eder:

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

Her profil için (`localhost`, `stage`, `k8s`), Git kimlik bilgileri environment variable'lardan okunur:

| Değişken | Açıklama |
|---|---|
| `GITHUB_USERNAME` | Config Git deposuna karşı kimlik doğrulamada kullanılan kullanıcı adı |
| `GITHUB_TOKEN` | Config Git deposuna karşı kimlik doğrulamada kullanılan personal access token |

`stock-management-configs` reposu **private** olduğu için, her iki değişken de Kubernetes dahil her ortamda set edilmelidir — anonim bir fallback yoktur.

## Çalıştırma

### Docker ile

```bash
docker build -t config-server .
docker run -p 8888:8888 \
  -e SPRING_PROFILES_ACTIVE=stage \
  -e GITHUB_USERNAME=kullanici-adiniz \
  -e GITHUB_TOKEN=token'iniz \
  config-server
```

### Maven ile

```bash
./mvnw spring-boot:run
```

### Health Check

Servis, Docker Compose tarafından kullanılan bir actuator health endpoint'i sunar:

```
GET http://localhost:8888/actuator/health
```

## Port

| Ortam | Port |
|---|---|
| Varsayılan | `8888` |

## Kullanan Servisler

Bu projedeki her servis, açılışta konfigürasyonunu bu sunucudan import eder, örn.:

```yaml
spring:
  config:
    import: "optional:configserver:http://config-server:8888"
```

## Kubernetes

Manifestler [`k8s/`](k8s) altında bulunur:

- `deployment.yaml` — Bu servis için Deployment. Image: `elifcelik49/sm-config-server:latest`. `SPRING_PROFILES_ACTIVE` değeri `k8s` olarak set edilir; `GITHUB_USERNAME`/`GITHUB_TOKEN` bir `github-secret` Secret'ından enjekte edilir (`kubectl create secret generic github-secret --from-literal=username=<kullanici> --from-literal=token=<pat>`).
- `service.yaml` — `config-server` adında bir `ClusterIP` Service, `8888` portunu açar. Servis adı tam olarak bu olmalı, çünkü diğer tüm servisler config server'ı Kubernetes DNS üzerinden, hardcoded/config-import edilmiş `http://config-server:8888` URL'i ile çözümlüyor.

Git PAT'ının, private `stock-management-configs` reposuna en azından okuma erişimi (classic token'lar için `repo` scope'u) olmalıdır.