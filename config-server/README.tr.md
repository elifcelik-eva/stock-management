# Config Server

[For English README click here](README.md)

[Stock Management System](../README.tr.md) projesinin bir parçasıdır. Bu servis, uzak bir Git deposundan diğer tüm servislere (`product-service`, `product-cache-service`, `spring-cloud-gateway`) merkezi konfigürasyon sunan bir **Spring Cloud Config Server**'dır.

## Sorumluluklar

- Diğer servislerin, her ortam için ayrı `application.yml` dosyası taşımak yerine ayarlarını açılışta HTTP üzerinden çekebilmesi için konfigürasyon dosyalarını sunar.
- Bir Git deposu (`stock-management-configs`) üzerinden beslenir; `clone-on-start` ve `force-pull` ayarları sayesinde her zaman en güncel commit edilmiş konfigürasyonu sunar.
- Birden fazla ortam profilini destekler: `localhost`, `stage` (ve bu servisi tüketen diğer servisler için `k8s`).

## Teknoloji Yığını

- Java 21
- Spring Boot 3.5.11
- Spring Cloud Config Server 2025.0.1

## Konfigürasyon

Servisin kendi `application.yaml` dosyası, paylaşılan konfigürasyon dosyalarının bulunduğu Git deposunu işaret eder:

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

`localhost` ve `stage` profilleri için Git kimlik doğrulama bilgileri ortam değişkenlerinden okunur:

| Değişken | Açıklama |
|---|---|
| `GITHUB_USERNAME` | Konfigürasyon Git deposuna kimlik doğrulamak için kullanılan kullanıcı adı |
| `GITHUB_TOKEN` | Konfigürasyon Git deposuna kimlik doğrulamak için kullanılan personal access token |

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

## Tüketiciler

Projedeki diğer tüm servisler, açılışta konfigürasyonlarını bu sunucudan import eder, örneğin:

```yaml
spring:
  config:
    import: "optional:configserver:http://config-server:8888"
```