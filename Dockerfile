# 1. AŞAMA: Projeyi Maven ile derleme (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Bağımlılıkları ve kaynak kodları kopyala
COPY pom.xml .
COPY src ./src

# Projeyi derle (testleri atlayıp hızlı build alıyoruz)
RUN mvn clean package -DskipTests

# 2. AŞAMA: Derlenen projenin çalıştırılması (Run)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Derlenen JAR dosyasını kopyala
COPY --from=build /app/target/*.jar app.jar

# Uygulama portu
EXPOSE 8080

# Başlatma komutu
ENTRYPOINT ["java", "-jar", "app.jar"]