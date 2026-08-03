# Product Service

[For English README click here](README.md)

[Stock Management System](../README.md) projesinin bir parçasıdır. Bu, sistemin ana servisi: `Product` domain'ine sahip, PostgreSQL üzerinde çalışan ve şeması **Flyway** ile yönetilen bir CRUD REST API'si sunar.

## Sorumluluklar

- Ürünler için tam CRUD (oluşturma, tekil getirme, tümünü listeleme, güncelleme, soft delete).
- Çoklu dil desteği (`EN`/`TR`) — her response'ta bir `language` path değişkeni üzerinden lokalize başarı/hata mesajları döner.
- Tutarlı bir response zarfı (`InternalApiResponse`): `payload`, opsiyonel bir `message` ve `hasError` bayrağı içerir.
- Veriyi `stock_management` şeması altında PostgreSQL'de tutar; şemanın kendisi Hibernate'in otomatik DDL'i yerine **Flyway** migration'larıyla versiyon kontrollüdür.
- Açılışta konfigürasyonunu `config-server`'dan çeker.
- Swagger/OpenAPI dokümantasyonu ve Docker/Kubernetes readiness için kullanılan bir actuator health endpoint'i sunar.

## Kullanılan Teknolojiler

- Java 21
- Spring Boot 3.2.5
- Spring Web, Spring Data JPA, Spring Cloud Config Client
- PostgreSQL (runtime), H2 (testler)
- **Flyway** (`flyway-core`) — şema migration'ları
- springdoc-openapi (Swagger UI)
- Lombok
- JUnit 5 / Spring Boot Test / Spring Security Test

## Veri Modeli

`Product` entity (`stock_management.product` tablosu):

| Alan | Tip | Notlar |
|---|---|---|
| `productId` | `long` | Primary key, otomatik üretilir |
| `productName` | `String` | Zorunlu |
| `quantity` | `Integer` | Zorunlu |
| `price` | `Double` | Zorunlu |
| `productCreatedDate` | `LocalDateTime` | Oluşturmada otomatik set edilir |
| `productUpdatedDate` | `LocalDateTime` | Oluşturma/güncellemede otomatik set edilir |
| `deleted` | `boolean` | Soft-delete bayrağı |

## Şema Yönetimi (Flyway)

Her profil (`localhost`, `stage`, `k8s`) Hibernate'i şu ayarla çalıştırır:

```yaml
jpa:
  hibernate:
    ddl-auto: validate
```

Hibernate hiçbir zaman tablo oluşturmaz veya değiştirmez — sadece entity mapping'inin veritabanındaki mevcut yapıyla uyumlu olduğunu doğrular. Gerçek tablo, versiyonlanmış bir migration script'i tarafından oluşturulur:

```
product-service/src/main/resources/db/migration/V1__create_product_table.sql
```

Flyway, uygulama her başladığında otomatik çalışır, uygulanan migration'ları bir `flyway_schema_history` tablosunda takip eder ve her `V<n>__açıklama.sql` dosyasını tam olarak bir kez uygular. Hedef şema, config server tarafından merkezi olarak sunulan şu ayarla belirlenir:

```yaml
spring:
  flyway:
    schemas: stock_management
    default-schema: stock_management
```

İleride yeni bir kolon veya tablo eklemek, yeni bir dosya eklemek anlamına gelir (örn. `V2__add_column.sql`) — mevcut migration dosyaları uygulandıktan sonra asla değiştirilmemelidir, çünkü Flyway bunların checksum'ını tutar.

## API

Temel path: `/api/1.0/product`

Tüm endpoint'ler response mesajlarını lokalize etmek için bir `{language}` path değişkeni (`EN` veya `TR`) gerektirir.

| Metod | Path | Açıklama |
|---|---|---|
| `POST` | `/{language}/products` | Yeni ürün oluşturur |
| `GET` | `/{language}/products/{productId}` | Tekil ürünü getirir |
| `GET` | `/{language}/products` | Tüm ürünleri listeler |
| `PUT` | `/{language}/products/{productId}` | Mevcut ürünü günceller |
| `DELETE` | `/{language}/products/{productId}` | Ürünü soft-delete yapar |

### Örnek: ürün oluşturma

```bash
curl -X POST http://localhost:9788/api/1.0/product/EN/products \
  -H "Content-Type: application/json" \
  -d '{
        "productName": "Wireless Mouse",
        "quantity": 100,
        "price": 19.99
      }'
```

### Response zarfı şekli

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

Özel exception'lar (`ProductNotFoundException`, `ProductAlreadyDeletedException`, `ProductNotCreateException`), `GlobalExceptionHandler` üzerinden merkezi olarak yakalanır ve lokalize, kullanıcı dostu hata mesajları döner.

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

Datasource ayarları (kullanıcı adı/şifre/URL, connection pool boyutları), config server üzerinden profil bazlı (`localhost`, `stage`, `k8s`) olarak şu environment variable'larla sağlanır:

| Değişken | Açıklama |
|---|---|
| `DB_USERNAME` | PostgreSQL kullanıcı adı |
| `DB_PASSWORD` | PostgreSQL şifresi |
| `CONFIG_SERVER_URL` | Config server'ın URL'i. Varsayılan `http://localhost:8888`'dir, bu yüzden config server'a localhost üzerinden erişilemeyen her ortamda (örn. Kubernetes: `http://config-server:8888`) **mutlaka** override edilmelidir |

## Çalıştırma

### Docker Compose ile (önerilen)
[Root README](../README.md)'e bakın — bu servis, tüm stack'in bir parçası olarak başlatılır.

### Bağımsız (Maven, localhost profili)

```bash
./mvnw spring-boot:run
```

`jdbc:postgresql://localhost:5433/stock_management` adresinden erişilebilir yerel bir PostgreSQL instance'ı (eşleşen container kurulumu için [`deployment/compose.yaml`](../deployment/compose.yaml) dosyasına bakın) ve çalışan bir config server gerektirir.

### Testler

```bash
./mvnw test
```

## Port

| Ortam | Port |
|---|---|
| Varsayılan | `9788` |

## Kubernetes

Manifestler [`k8s/`](k8s) altında bulunur:

- `product-service/product-deployment.yaml`, `product-service/product-service.yaml` — Bu uygulama için Deployment ve Service. Image: `elifcelik49/sm-product-service:latest`. Gerekli environment variable'lar:
    - `SPRING_PROFILES_ACTIVE=k8s`
    - `CONFIG_SERVER_URL=http://config-server:8888`
    - `DB_USERNAME` / `DB_PASSWORD` — `postgres-secret` Secret'ından enjekte edilir
- `postgres/postgres-deployment.yaml`, `postgres/postgres-service.yaml`, `postgres/postgres-init-configmap.yaml` — PostgreSQL Deployment, Service ve şema-init ConfigMap. Kimlik bilgileri (`database`, `username`, `password`) de `postgres-secret`'tan enjekte edilir.

```bash
kubectl create secret generic postgres-secret \
  --from-literal=database=stock_management \
  --from-literal=username=postgres \
  --from-literal=password=postgres123

kubectl apply -f k8s/postgres/
kubectl apply -f k8s/product-service/
```

Açılışta Flyway, Hibernate doğrulama yapmadan önce `V1__create_product_table.sql`'i `stock_management` şemasına (Postgres init ConfigMap'i tarafından önceden oluşturulmuş) uygular — `ddl-auto: validate`'in bu ortamda güvenle kullanılabilmesini sağlayan şey tam olarak budur.