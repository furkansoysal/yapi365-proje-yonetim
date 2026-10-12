# Yapı365 — Rol Bazlı Proje Yönetim Yazılımı

Java 21 · Spring Boot 3 · Thymeleaf · PostgreSQL · Docker · Microsoft Graph

**Canlı arayüz demosu:** https://furkansoysal.github.io/yapi365-proje-yonetim/ (tüm modüller, rol değiştirme, onay akışları, chatbot)

Bu repo, Microsoft 365 entegrasyonlu proje yönetim sisteminin **çalışan iskeletidir**. Mimarinin kritik parçaları uçtan uca kuruludur; modüller bu iskeletin üzerine aynı desenle eklenir.

## İskelette çalışan parçalar

| Parça | Durum | Nerede |
|---|---|---|
| Microsoft 365 ile giriş (SSO, Entra ID / OIDC) | ✅ | `EntraSecurityConfig`, `EntraOidcUserService` |
| Rol bazlı yetki: modül × (Görüntüle / Düzenle / Onayla), arayüzden yönetilen matris | ✅ | `AppRole`, `Authorities`, `/admin/roles` |
| 10 hazır rol | ✅ | `DataSeeder` |
| Çoklu proje (kullanıcı-proje ataması, proje seçici) | ✅ | `ProjectAccessService` |
| Kayıt geçmişi: kim, ne zaman, hangi alanı neyden neye değiştirdi | ✅ | Hibernate Envers, `AuditService`, `/admin/audit` |
| Doküman yönetimi: klasör, versiyon, SHA-256 | ✅ | `DocumentService`, `/documents` |
| SharePoint/OneDrive'a otomatik aktarım (büyük dosyalar parça parça yüklenir) ve hata olursa tekrar deneme | ✅ | `GraphDriveClient`, `DocumentSyncService` |
| Sözleşmeler: 4 tür, kritik tarih ve teminat takibi | ✅ | `/contracts` |
| Mail otomasyonu: paylaşılan kutu okuma, kural motoru, LLM sınıflandırma arayüzü | ✅ | `mail/*`, `/mail` |
| Responsive arayüz | ✅ | `static/css/app.css` |
| Docker Compose ile kurulum | ✅ | `docker-compose.yml` |

## Hızlı başlangıç (demo)

```bash
cp .env.example .env
docker compose up --build
```

Ardından http://localhost:8080 adresini açın. Demo kullanıcılar giriş ekranında listelenir, hepsinin şifresi `demo`. Farklı rollerle girdiğinizde menülerin ve yetkilerin nasıl değiştiğini görebilirsiniz.

## Canlı ortam: Microsoft 365

1. **Entra ID → App registrations → New registration**
   - Redirect URI (Web): `https://<alan-adı>/login/oauth2/code/azure`
   - Certificates & secrets bölümünden bir client secret oluşturun.
2. **API permissions** (Application, admin consent):
   - `Files.ReadWrite.All` (veya yalnızca ilgili site için `Sites.Selected`)
   - `Mail.ReadWrite`. Erişimi yalnızca paylaşılan kutuyla sınırlamak için Exchange *Application Access Policy* kullanın.
3. `.env` dosyasında şu ayarları yapın:
   - `SPRING_PROFILES_ACTIVE=entra` ve `ENTRA_*` değerleri
   - `GRAPH_ENABLED=true`, `GRAPH_*` değerleri
   - `DEMO_DATA=false`
4. Kullanıcıları ve rolleri sistemde tanımlayın. Giriş Microsoft 365 hesabıyla yapılır; yetkiler uygulamadaki rol matrisinden gelir.

## Mimari notlar

- **Dosyalar önce sunucuya, sonra SharePoint'e gider.** Yükleme hızlı tamamlanır; aktarım commit sonrasında arka planda yapılır. Microsoft 365 geçici olarak erişilemezse sistem çalışmaya devam eder, aktarım daha sonra tekrar denenir.
- **4 MB üstü dosyalar** Graph *upload session* ile 10 MiB'lık parçalar halinde yüklenir. Dosya belleğe alınmaz.
- **Yetki kontrolü iki katmanlıdır.** Controller metodlarında `@PreAuthorize("hasAuthority('SOZLESME:EDIT')")` kontrolü yapılır. Şablonlarda `sec:authorize` ile menüler ve butonlar gizlenir.
- **Şema Flyway ile versiyonlanır.** `src/main/resources/db/migration` klasöründedir.

## Test

```bash
mvn verify
```

Testler, kural motorunu, rol bazlı erişimi (403 kontrolleri), sözleşme değişiklik geçmişini ve doküman versiyonlamayı kapsar.
