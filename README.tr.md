# Stock Management System (Stok Yönetim Sistemi)

[For English README click here](README.md)

**Spring Boot** ve **Spring Cloud** ile geliştirilmiş, mikroservis mimarisine sahip bir stok/envanter yönetim sistemi. Proje; merkezi konfigürasyon yönetimi, bir API gateway, ilişkisel veriye dayalı bir servis ve bu servisle Feign üzerinden haberleşen Redis destekli bir önbellekleme servisinden oluşan gerçekçi bir mikroservis mimarisini örnekler. Tüm sistem Docker Compose ile ayağa kaldırılabilir, Kubernetes üzerine de deploy edilebilir.

## Mimari

```
                          ┌─────────────────────┐
                          │   Config Server      │
                          │  (Spring Cloud Config)│
                          └──────────┬───────────┘
                                     │ konfigürasyon
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
   ┌──────────▼─────────┐ ┌──────────▼─────────┐ ┌──────────▼─────────┐
   │ Spring Cloud Gateway│ │  Product Service    │ │ Product Cache Svc   │
   │   (uç yönlendirme)  │─▶  (PostgreSQL, CRUD) │◀─┤  (Redis + Feign)   │
   └──────────────────────┘ └──────────┬──────────┘ └──────────┬─────────┘
                                       │                        │
                              ┌────────▼────────┐      ┌────────▼────────┐
                              │   PostgreSQL     │      │      Redis      │
                              └──────────────────┘      └──────────────────┘
```

## Servisler

| Servis | Açıklama | Varsayılan Port | README |
|---|---|---|---|
| [`config-server`](config-server) | Bir Git deposu üzerinden beslenen, merkezi konfigürasyon servisi (Spring Cloud Config) | `8888` | [EN](config-server/README.md) / [TR](config-server/README.tr.md) |
| [`spring-cloud-gateway`](spring-cloud-gateway) | Dış trafiği iç servislere yönlendiren API gateway | `8762` | [EN](spring-cloud-gateway/README.md) / [TR](spring-cloud-gateway/README.tr.md) |
| [`product-service`](product-service) | PostgreSQL üzerinde çalışan temel ürün CRUD servisi | `9788` | [EN](product-service/README.md) / [TR](product-service/README.tr.md) |
| [`product-cache-service`](product-cache-service) | `product-service` önünde, Redis destekli önbellekleme servisi | `9791` | [EN](product-cache-service/README.md) / [TR](product-cache-service/README.tr.md) |

Ek klasörler:
- [`configs`](configs) — Config server tarafından sunulan Spring Cloud Config profil dosyaları (`localhost`, `stage`, `k8s`).
- [`deployment`](deployment) — Sistemi yerelde ayağa kaldırmak için Docker Compose dosyası ve PostgreSQL init script'leri.

## Teknoloji Yığını

- **Java 21**
- **Spring Boot** (servise göre 3.2.x / 3.5.x)
- **Spring Cloud** — Config Server, Gateway, OpenFeign
- **PostgreSQL 16** — `product-service` için kalıcı veri katmanı
- **Redis 7** — `product-cache-service` için önbellekleme katmanı
- **Docker & Docker Compose** — yerel orkestrasyon
- **Kubernetes manifest'leri** — `product-service` ve `spring-cloud-gateway` için mevcut
- **springdoc-openapi (Swagger UI)** — API dokümantasyonu
- **Lombok**, **JPA/Hibernate**, **Maven**

## Başlarken

### Ön Gereksinimler
- Docker & Docker Compose
- JDK 21 (yalnızca servisleri Docker dışında build edip çalıştırmak isterseniz gerekli)
- Bir GitHub personal access token, çünkü config server konfigürasyonu bir Git deposundan çekiyor

### Docker Compose ile Çalıştırma

1. `deployment/compose.yaml` ile aynı klasöre gerekli değişkenleri içeren bir `.env` dosyası oluşturun:

```env
GITHUB_USERNAME=github-kullanici-adiniz
GITHUB_TOKEN=github-token'iniz
DB_NAME=stock_management
DB_USERNAME=postgres
DB_PASSWORD=postgres
REDIS_PASSWORD=redis-sifreniz
CONFIG_SERVER_URL=http://config-server:8888
```

2. `deployment` klasöründen sistemi başlatın:

```bash
cd deployment
docker compose up --build
```

3. Servisler şu adreslerde erişilebilir olacaktır:
    - Config Server → `http://localhost:8888`
    - Gateway → `http://localhost:8762`
    - Product Service → `http://localhost:9788`
    - Product Cache Service → `http://localhost:9791`
    - PostgreSQL → `localhost:5433`
    - Redis → `localhost:6379`

Başlangıç sırası Docker Compose health check'leri ile garanti altına alınmıştır: bağımlı servisler başlamadan önce `config-server` ile `postgres`/`redis`'in sağlıklı (healthy) olması beklenir.

### Yerelde Çalıştırma (Docker olmadan)

Her servis, kendi `localhost` Spring profiliyle tek tek de çalıştırılabilir — detaylar için her servisin kendi README dosyasına bakın.

## Konfigürasyon Yönetimi

Config server hariç tüm servisler, açılışta konfigürasyonlarını `config-server`'dan import eder:

```yaml
spring:
  config:
    import: "optional:configserver:http://config-server:8888"
```

Config server ise YAML dosyalarını ayrı bir Git deposundan (`stock-management-configs`) çeker — bu repodaki [`configs`](configs) klasörü, o Git deposunda bulunması beklenen içeriği; `localhost`, `stage` ve `k8s` profillerine özel bölümleriyle birlikte yansıtır.

## Kubernetes

`product-service` ve `spring-cloud-gateway`, Deployment ve Service tanımlarını içeren Kubernetes manifest'lerine (`k8s/`) sahiptir; `product-service` için ayrıca bir PostgreSQL Deployment/Service/ConfigMap de bulunur. Bunlar `k8s` Spring profili için tasarlanmıştır.

## Notlar

Bu proje; config server, gateway, Feign üzerinden servis haberleşmesi ve önbellekleme gibi mikroservis mimarisi pratiklerini Spring Cloud, Docker ve Kubernetes ile denemek amacıyla geliştirilmiş kişisel/öğrenme amaçlı bir projedir.