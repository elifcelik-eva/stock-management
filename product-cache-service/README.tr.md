# Product Cache Service

[For English README click here](README.md)

[Stock Management System](../README.tr.md) projesinin bir parçasıdır. Bu servis, `product-service`'in önünde yer alır ve tekrar eden okuma işlemlerinde veritabanına giden yükü azaltmak için Redis destekli bir ürün önbelleği sağlar.

## Sorumluluklar

- Henüz önbellekte olmayan ürün verilerini, bir **Feign client** (`ProductServiceFeignClient`) üzerinden `product-service`'ten çeker.
- Ürünleri **Redis**'te önbellekler ve sonraki okumaları önbellekten sunar.
- Dile göre önbellekteki ürünleri temizlemek/tahliye etmek için bir endpoint sunar.
- Sistemin genelinde kullanılan yerelleştirilmiş (`EN`/`TR`), tutarlı yanıt zarfı stilini kullanır.
- `product-service`'e ulaşılamadığı durumları özel bir `ProductServiceUnavailableException` ile ele alır.
- Açılışta konfigürasyonunu, `fail-fast` ve retry mekanizması etkin olacak şekilde `config-server`'dan çeker.

## Teknoloji Yığını

- Java 21
- Spring Boot 3.2.5
- Spring Web, Spring Data Redis
- Spring Cloud OpenFeign, Spring Cloud Config Client
- springdoc-openapi (Swagger UI)
- Lombok

## API

Ana yol (base path): `/api/1.0/product-cache`

| Metod | Yol | Açıklama |
|---|---|---|
| `GET` | `/{language}/products/{productId}` | ID'ye göre ürün getirir — Redis önbelleğinden sunulur, önbellekte yoksa `product-service`'e düşer |
| `DELETE` | `/{language}/products` | Önbellekteki tüm ürünleri tahliye eder |

### Örnek: önbellekteki bir ürünü getirme

```bash
curl http://localhost:9791/api/1.0/product-cache/tr/products/1
```

## Nasıl Çalışır

1. ID'ye göre bir ürün isteği gelir.
2. `ProductService`, Redis'te önbelleklenmiş bir kayıt olup olmadığını kontrol eder.
3. Önbellekte bulunmazsa (cache miss), `product-service`'i `ProductServiceFeignClient` üzerinden çağırır (mantıksal ad olarak `product-service` altında kayıtlıdır, gerçek URL'i her ortam için config server üzerinden çözümlenir).
4. Sonuç map'lenir ve sonraki istekler için Redis'e kaydedilir.

```
İstemci → Product Cache Service → Redis (var mı?)
                                └─(yok)→ product-service (Feign) → Redis (kaydet)
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
| `REDIS_PASSWORD` | Redis örneği için şifre |

| Profil | Redis host | `product-service` Feign URL |
|---|---|---|
| `localhost` | `localhost:6379` | `http://localhost:9788` |
| `stage` | `redis:6379` | `http://product-service:9788` |

## Çalıştırma

### Docker Compose ile (önerilen)
[Root README](../README.tr.md)'ye bakın. Bu servis, önce Redis, `product-service` ve `config-server`'ın sağlıklı (healthy) olmasına bağımlıdır.

### Tek başına (Maven, localhost profili)

```bash
./mvnw spring-boot:run
```

Yerel bir Redis örneği, erişilebilir bir `product-service` ve çalışan bir config server gerektirir.

## Port

| Ortam | Port |
|---|---|
| Varsayılan | `9791` |