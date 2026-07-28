# Spring Cloud Gateway

[For English README click here](README.md)

[Stock Management System](../README.tr.md) projesinin bir parçasıdır. Bu, sistemin API gateway'idir: dış istekleri iç servis olan `product-service`'e yönlendiren tek giriş noktasıdır.

## Sorumluluklar

- `/api/1.0/product/**` ile eşleşen gelen HTTP isteklerini `product-service`'e yönlendirir.
- Sistemin dış yüzünü merkezileştirir; böylece istemcilerin yalnızca tek bir adresi bilmesi yeterlidir.
- Açılışta yönlendirme (routing) konfigürasyonunu `config-server`'dan çeker; bu sayede route'lar, gateway yeniden deploy edilmeden ortama göre değiştirilebilir.
- Docker/Kubernetes tarafından kullanılan bir actuator health endpoint'i sunar.

## Teknoloji Yığını

- Java 21
- Spring Boot 3.2.5
- Spring Cloud Gateway
- Spring Cloud Config Client
- Spring Boot Actuator

## Yönlendirme (Routing)

Route'lar, paylaşılan konfigürasyonda (`config-server` tarafından sunulur) Spring profili bazında tanımlanır:

| Profil | Route id | Predicate | Hedef URI |
|---|---|---|---|
| `localhost` | `product-service` | `Path=/api/1.0/product/**` | `http://localhost:9788` |
| `stage` | `product-service` | `Path=/api/1.0/product/**` | `http://product-service:9788` |
| `k8s` | `product-service` | `Path=/api/1.0/product/**` | `http://product-service:9788` |

### Örnek

```bash
# Gateway üzerinden
curl http://localhost:8762/api/1.0/product/tr/products

# product-service'e doğrudan eşdeğer çağrı
curl http://localhost:9788/api/1.0/product/tr/products
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
[Root README](../README.tr.md)'ye bakın. Bu servis, `product-service` ve `config-server` sağlıklı (healthy) hale geldikten sonra başlar.

### Tek başına (Maven, localhost profili)

```bash
./mvnw spring-boot:run
```

Erişilebilir bir `product-service` ve çalışan bir config server gerektirir.

## Port

| Ortam | Port |
|---|---|
| Varsayılan | `8762` |

## Kubernetes

[`k8s/`](k8s) altında, `k8s` Spring profili için tasarlanmış, bu uygulama için Deployment ve Service içeren manifest'ler bulunur.