# 🗂️☁️ Облачное хранилище файлов

## 🔙 Бэкенд

**Технологии:**
- Java 17+
- Spring Boot
- Spring Web MVC
- Spring Security
- Spring Sessions
- Lombok
- MapStruct
- Swagger / OpenAPI 3
- Gradle

## 🗄️ Базы данных и хранилища

- PostgreSQL – основная реляционная база данных
- Redis – хранилище сессий
- MinIO – S3-совместимое файловое хранилище
- Spring Data JPA – взаимодействие с базой данных
- Liquibase – управление миграциями

## 🧪 Тестирование

- JUnit 5 – модульные тесты
- Spring Test – тестирование компонентов Spring
- Testcontainers – интеграционные тесты с изолированной средой

---

## ⚙️ Установка проекта

1. Склонируйте репозиторий:

```
git clone https://github.com/fernerman/cloudfilestorage.git
```
2. Откройте папку проекта в IntelliJ IDEA.

3. В корне проекта создайте файл .env и заполните его по шаблону:
 ```
DB_NAME=
DB_URL=
DB_USER=
DB_PASSWORD=

MINIO_ROOT_USER=
MINIO_ROOT_PASSWORD=
MINIO_BUCKET_NAME=user-files
MINIO_URL=

REDIS_HOST=
```
