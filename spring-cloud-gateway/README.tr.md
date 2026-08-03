# Spring Cloud Gateway

[For English README click here](README.md)

[Stock Management System](../README.md) projesinin bir parçasıdır. Bu, sistemin API gateway'i: dış istekleri iç servisler olan `product-service` ve `product-cache-service`'e yönlendiren tek giriş noktası.

## Sorumluluklar

- `/api/1.0/product/**` ile eşleşen gelen HTTP isteklerini `product-service`'e, `/api/1.0/product-cache/**` ile eşleşenleri ise `product-cache-service`'e yönlendirir.
- Sistemin dış ucunu merkezileştirir; istemcilerin sadece tek bir adresi bilmesi yeterlidir.
- Açılışta routing konfigürasyonunu `config-server`'dan çeker — bu sayede route'lar, gateway'i yeniden deploy etmeden ortama göre değiştirilebilir.
- Docker/Kubernetes tarafından kullanılan bir actuator health endpoint'i sunar.

## Kullanılan Teknolojiler

- Java 21
- Spring Boot 3.2.5
- Spring Cloud Gateway
- Spring Cloud Config Client
- Spring Boot Actuator

## Routing

Route'lar, paylaşılan konfigürasyonda (config-server tarafından sunulan) her Spring profili için ayrı ayrı tanımlıdır:

| Profil | Route id | Predicate | Hedef URI |
|---|---|---|---|
| `localhost` | `product-service` | `Path=/api/1.0/product/**` | `http://localhost:9788` |
| `localhost` | `product-cache-service` | `Path=/api/1.0/product-cache/**` | `http://localhost:9791` |
| `stage` | `product-service` | `Path=/api/1.0/product/**` | `http://product-service:9788` |
| `stage` | `product-cache-service` | `Path=/api/1.0/product-cache/**` | `http://product-cache-service:9791` |
| `k8s` | `product-service` | `Path=/api/1.0/product/**` | `http://product-service:9788` |
| `k8s` | `product-cache-service` | `Path=/api/1.0/product-cache/**` | `http://product-cache-service:9791` |

Route `id`'leri benzersiz olmalıdır; Spring Cloud Gateway route'ları sırayla değerlendirir ve predicate'i ilk eşleşene yönlendirir.

### Örnek

```bash
# Gateway üzerinden → product-service
curl http://localhost:8762/api/1.0/product/EN/products

# Gateway üzerinden → product-cache-service
curl http://localhost:8762/api/1.0/product-cache/EN/products/1

# product-service'e eşdeğer doğrudan çağrı
curl http://localhost:9788/api/1.0/product/EN/products
```

## Konfigürasyon

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

## Çalıştırma

### Docker Compose ile (önerilen)
[Root README](../README.md)'e bakın. Bu servis, `product-service` ve `config-server` sağlıklı olduktan sonra başlar.

### Bağımsız (Maven, localhost profili)

```bash
./mvnw spring-boot:run
```

Erişilebilir bir `product-service` (cache route'unu test ediyorsan `product-cache-service` de) ve çalışan bir config server gerektirir.

## Port

| Ortam | Port |
|---|---|
| Varsayılan | `8762` |

## Kubernetes

Manifestler [`k8s/`](k8s) altında bulunur:

- `deployment.yaml` — Bu servis için Deployment. Image: `elifcelik49/sm-gateway:latest`. `SPRING_PROFILES_ACTIVE=k8s`.
- `service.yaml` — Cluster dışına açılan tek giriş noktası olan `NodePort` tipinde bir Service (`8762` portu, `nodePort: 30007`). Minikube'da erişilebilir bir URL almak için:

```bash
minikube service spring-cloud-gateway --url
```

```bash
curl http://<minikube-url>/api/1.0/product/EN/products
curl http://<minikube-url>/api/1.0/product-cache/EN/products/1
```