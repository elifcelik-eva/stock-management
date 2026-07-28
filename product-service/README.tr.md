# Product Service

[For English README click here](README.md)

[Stock Management System](../README.tr.md) projesinin bir parçasıdır. Bu servis, sistemin çekirdek servisidir: `Product` (ürün) alanının sahibidir ve PostgreSQL üzerinde ürün oluşturma, okuma, güncelleme ve silme işlemleri için bir REST API sunar.

## Sorumluluklar

- Ürünler için tam CRUD desteği (oluşturma, tekil getirme, tümünü listeleme, güncelleme, soft delete).
- Her yanıtta, `language` path değişkeni üzerinden döndürülen yerelleştirilmiş (çok dilli) başarı/hata mesajları — `EN` ve `TR`.
- Tutarlı bir yanıt zarfı (`InternalApiResponse`): bir `payload`, opsiyonel bir kullanıcı dostu `message` ve bir `hasError` bayrağı içerir.
- Verileri PostgreSQL'de `stock_management` şeması altında kalıcı hale getirir.
- Açılışta konfigürasyonunu `config-server`'dan çeker.
- Swagger/OpenAPI dokümantasyonu ve Docker/Kubernetes readiness kontrolleri için kullanılan bir actuator health endpoint'i sunar.

## Teknoloji Yığını

- Java 21
- Spring Boot 3.2.5
- Spring Web, Spring Data JPA, Spring Cloud Config Client
- PostgreSQL (çalışma zamanı), H2 (testler)
- springdoc-openapi (Swagger UI)
- Lombok
- JUnit 5 / Spring Boot Test / Spring Security Test

## Veri Modeli

`Product` entity'si (`stock_management.product` tablosu):

| Alan | Tip | Notlar |
|---|---|---|
| `productId` | `long` | Primary key, otomatik üretilir |
| `productName` | `String` | Zorunlu |
| `quantity` | `Integer` | Zorunlu |
| `price` | `Double` | Zorunlu |
| `productCreatedDate` | `LocalDateTime` | Oluşturmada otomatik atanır |
| `productUpdatedDate` | `LocalDateTime` | Oluşturma/güncellemede otomatik atanır |
| `deleted` | `boolean` | Soft-delete bayrağı |

## API

Ana yol (base path): `/api/1.0/product`

Tüm endpoint'ler, yanıt mesajlarını yerelleştirmek için kullanılan bir `{language}` path değişkeni gerektirir — `en` veya `tr`.

| Metod | Yol | Açıklama |
|---|---|---|
| `POST` | `/{language}/products` | Yeni ürün oluşturur |
| `GET` | `/{language}/products/{productId}` | ID'ye göre tekil ürün getirir |
| `GET` | `/{language}/products` | Tüm ürünleri listeler |
| `PUT` | `/{language}/products/{productId}` | Var olan bir ürünü günceller |
| `DELETE` | `/{language}/products/{productId}` | Bir ürünü soft-delete yapar |

### Örnek: ürün oluşturma

```bash
curl -X POST http://localhost:9788/api/1.0/product/tr/products \
  -H "Content-Type: application/json" \
  -d '{
        "productName": "Kablosuz Mouse",
        "quantity": 100,
        "price": 19.99
      }'
```

### Yanıt zarfı yapısı

```json
{
  "message": {
    "title": "Başarılı",
    "description": "Ürün başarıyla oluşturuldu"
  },
  "httpStatus": "CREATED",
  "hasError": false,
  "payload": {
    "productId": 1,
    "productName": "Kablosuz Mouse",
    "quantity": 100,
    "price": 19.99,
    "productCreatedDate": "2026-07-28T10:00:00",
    "productUpdatedDate": "2026-07-28T10:00:00"
  }
}
```

Özel exception'lar (`ProductNotFoundException`, `ProductAlreadyDeletedException`, `ProductNotCreateException`), `GlobalExceptionHandler` üzerinden merkezi olarak yakalanır ve yerelleştirilmiş, kullanıcı dostu hata mesajları döndürülür.

## Konfigürasyon

Servis, konfigürasyonunu config server'dan import eder:

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

Datasource ayarları (kullanıcı adı/şifre/URL, connection pool boyutları), config server üzerinden profil bazlı (`localhost`, `stage`, `k8s`) olarak, aşağıdaki ortam değişkenleri kullanılarak sağlanır:

| Değişken | Açıklama |
|---|---|
| `DB_USERNAME` | PostgreSQL kullanıcı adı |
| `DB_PASSWORD` | PostgreSQL şifresi |
| `CONFIG_SERVER_URL` | Config server URL'i |

## Çalıştırma

### Docker Compose ile (önerilen)
[Root README](../README.tr.md)'ye bakın — bu servis, tüm sistemin bir parçası olarak başlatılır.

### Tek başına (Maven, localhost profili)

```bash
./mvnw spring-boot:run
```

`jdbc:postgresql://localhost:5433/stock_management` adresinden erişilebilir yerel bir PostgreSQL örneği (eşleşen container kurulumu için [`deployment/compose.yaml`](../deployment/compose.yaml) dosyasına bakın) ve çalışan bir config server gerektirir.

### Testler

```bash
./mvnw test
```

## Port

| Ortam | Port |
|---|---|
| Varsayılan | `9788` |

## Kubernetes

Manifest'ler [`k8s/`](k8s) altında bulunur:
- `product-service/product-deployment.yaml`, `product-service/product-service.yaml` — bu uygulama için Deployment ve Service.
- `postgres/postgres-deployment.yaml`, `postgres/postgres-service.yaml`, `postgres/postgres-init-configmap.yaml` — PostgreSQL Deployment, Service ve şema-init ConfigMap'i.