# Product Cache Service

[For English README click here](README.md)

[Stock Management System](../README.md) projesinin bir parçasıdır. Bu servis, `product-service`'in önünde durur ve tekrarlanan sorgularda ana veritabanı servisinin yükünü azaltmak için Redis destekli bir önbellekleme katmanı sağlar.

## Sorumluluklar

- Önbellekte yoksa, ürün verisini **Feign client** (`ProductServiceFeignClient`) üzerinden `product-service`'ten çeker.
- Ürünleri **Redis**'te önbelleğe alır ve sonraki okumaları önbellekten sunar.
- Dile göre önbellekteki ürünleri temizleyen/tahliye eden bir endpoint sunar.
- Sistemin geri kalanında kullanılan aynı lokalize (`EN`/`TR`), tutarlı response zarfı biçimini döner.
- `product-service`'e ulaşılamama durumunu özel bir `ProductServiceUnavailableException` ile yönetir.
- Açılışta konfigürasyonunu `config-server`'dan çeker; `fail-fast` ve retry etkindir.

## Kullanılan Teknolojiler

- Java 21
- Spring Boot 3.2.5
- Spring Web, Spring Data Redis
- Spring Cloud OpenFeign, Spring Cloud Config Client
- springdoc-openapi (Swagger UI)
- Lombok

## API

Temel path: `/api/1.0/product-cache`

| Metod | Path | Açıklama |
|---|---|---|
| `GET` | `/{language}/products/{productId}` | Ürünü id ile getirir — Redis önbelleğinden sunulur, cache miss durumunda `product-service`'e düşer |
| `DELETE` | `/{language}/products` | Önbellekteki tüm ürünleri temizler |

### Örnek: önbellekten bir ürün getirme

```bash
curl http://localhost:9791/api/1.0/product-cache/EN/products/1
```

## Nasıl Çalışır

1. Bir ürün için id bazlı bir istek gelir.
2. `ProductService`, Redis'te önbelleğe alınmış bir kayıt olup olmadığını kontrol eder.
3. Cache miss durumunda, `ProductServiceFeignClient` üzerinden `product-service`'i çağırır (mantıksal isim `product-service` olarak kayıtlıdır, gerçek URL'e her ortamda config server aracılığıyla çözümlenir).
4. Sonuç map'lenip sonraki istekler için Redis'e kaydedilir.

```
İstemci → Product Cache Service → Redis (hit?) 
                              └─(miss)→ product-service (Feign) → Redis (kaydet)
```

## Konfigürasyon

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

Redis bağlantısı ve `product-service` için Feign hedef URL'i, config server üzerinden profil bazlı olarak sağlanır:

| Değişken | Açıklama |
|---|---|
| `REDIS_PASSWORD` | Redis instance'ının şifresi |

| Profil | Redis host | `product-service` Feign URL |
|---|---|---|
| `localhost` | `localhost:6379` | `http://localhost:9788` |
| `stage` | `redis:6379` | `http://product-service:9788` |
| `k8s` | `redis:6379` | `http://product-service:9788` |

## Çalıştırma

### Docker Compose ile (önerilen)
[Root README](../README.md)'e bakın. Bu servis, önce Redis, `product-service` ve `config-server`'ın sağlıklı (healthy) olmasına bağımlıdır.

### Bağımsız (Maven, localhost profili)

```bash
./mvnw spring-boot:run
```

Yerel bir Redis instance'ı, erişilebilir bir `product-service` ve çalışan bir config server gerektirir.

## Port

| Ortam | Port |
|---|---|
| Varsayılan | `9791` |

## Kubernetes

Manifestler [`k8s/`](k8s) altında bulunur:

- `product-cache-service/deployment.yaml`, `product-cache-service/service.yaml` — Bu uygulama için Deployment ve Service. Image: `elifcelik49/sm-product-cache-service:latest`. Gerekli environment variable'lar:
    - `SPRING_PROFILES_ACTIVE=k8s`
    - `REDIS_PASSWORD` — `redis-secret` Secret'ından enjekte edilir
- `redis/redis-deployment.yaml`, `redis/redis-service.yaml` — Bir `redis:7` Deployment'ı (şifre, `command` alanında Kubernetes'in kendi `$(VAR)` substitution mekanizmasıyla — shell expansion değil — `redis-server --requirepass $(REDIS_PASSWORD)` şeklinde set edilir) ve `k8s` config profilinde beklenen `data.redis.host: redis` değeriyle eşleşen, `redis` adında bir `ClusterIP` Service.

```bash
kubectl create secret generic redis-secret \
  --from-literal=password=redis123

kubectl apply -f k8s/redis/
kubectl apply -f k8s/product-cache-service/
```

Bu servise [gateway](../spring-cloud-gateway) üzerinden erişebilmek için `/api/1.0/product-cache/**` route'unun tanımlı olması gerektiğini unutmayın — detaylar için gateway'in README dosyasına bakın.