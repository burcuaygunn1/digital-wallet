# 💳 Digital Wallet API & Platform

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?style=for-the-badge&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-red?style=for-the-badge&logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker)

Güvenli kullanıcı kimlik doğrulaması, çoklu para birimi yönetimi, bakiyeler arası para transferi ve döviz çevrim işlemlerini destekleyen **RESTful Dijital Cüzdan Uygulaması**.

---

## 📌 Proje Hakkında (About)

**Digital Wallet Platform**, modern finansal uygulamaların temel ihtiyaçlarını karşılamak üzere katmanlı mimari (Layered Architecture) prensiplerine uygun olarak geliştirilmiş full-stack bir backend sistemidir. 

Proje; kullanıcı güvenliği (JWT & Spring Security 6), hesap ve bakiye yönetimi, dinamik kur çevrimli transfer işlemleri ve konteynerleştirme (Docker) süreçlerini kapsayan 4 ana fazda tamamlanmıştır.

---

## 🚀 Öne Çıkan Özellikler (Features)

* **Kimlik Doğrulama & Güvenlik:**
  * JWT (JSON Web Token) tabanlı durumsuz (stateless) authentication/authorization.
  * Spring Security 6.x konfigürasyonları ve özelleştirilmiş `JwtAuthenticationFilter`.
  * Rol ve izin tabanlı yetkilendirme altyapısı.
* **Hesap & Bakiye Yönetimi:**
  * Kullanıcıya özel çoklu para birimi (TRY, USD, EUR vb.) desteğiyle cüzdan oluşturma.
  * Para yükleme (Deposit) ve para çekme (Withdraw) işlemleri.
* **Transfer & Kur Çevrimi:**
  * Kullanıcılar arası veya hesaplar arası anlık bakiye transferi.
  * Farklı para birimleri arasında otomatik dönüştürme mantığı.
  * Tüm finansal hareketlerin izlenebilirliği için işlem geçmişi (Transaction History) kaydı.
* **Konteynerleştirme & Taşınabilirlik:**
  * `Dockerfile` ve `docker-compose.yml` ile tek komutla ayağa kaldırılabilen izole mikro-mimari (Spring Boot + PostgreSQL).

---

## 🛠️ Kullanılan Teknolojiler (Tech Stack)

* **Backend:** Java 21, Spring Boot 3, Spring Data JPA, Spring Security 6, JWT (jjwt)
* **Veritabanı:** PostgreSQL
* **Frontend:** HTML5, JavaScript (ES6+), Tailwind CSS
* **DevOps & Araçlar:** Docker, Docker Compose, Maven, Git, Postman / Swagger

---

## ⚡ Kurulum ve Çalıştırma (Getting Started)

Projeyi yerel ortamınızda çalıştırmak için bilgisayarınızda **Docker Desktop** yüklü olması yeterlidir. (Java veya PostgreSQL kurulumu gerekmez).

### 1. Repoyu Klonlayın
```bash
git clone [https://github.com/burcuaygunn1/digital-wallet.git](https://github.com/burcuaygunn1/digital-wallet.git)
cd digital-wallet
