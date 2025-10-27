🏦 Система управления банковскими картами 

📋 Обзор проекта \
Безопасное REST API для управления банковскими картами с аутентификацией, ролевым доступом и операциями перевода средств между картами.

🚀 Возможности

🔐 Аутентификация и безопасность
JWT аутентификация с refresh токенами

Ролевой доступ (USER/ADMIN)

Шифрование паролей и валидация

Маскирование конфиденциальных данных

👥 Управление пользователями
Регистрация и аутентификация пользователей

Разрешения на основе ролей

Управление профилями

💳 Операции с картами
Создание и управление банковскими картами

Просмотр карт с пагинацией и фильтрацией

Блокировка/разблокировка карт

Проверка баланса

Безопасное маскирование номеров карт

💸 Транзакции
Переводы между картами пользователя

Валидация транзакций

Проверка баланса

🛡️ Административные функции
Полное управление картами

Администрирование пользователей

Контроль за системой

🏗️ Архитектура
Технологический стек
Java 17+ - Основной язык программирования

Spring Boot 3.x - Фреймворк приложения

Spring Security - Аутентификация и авторизация

Spring Data JPA - Работа с базой данных

PostgreSQL - Основная база данных

Liquibase - Миграции базы данных

Docker - Контейнеризация

JWT - Токены аутентификации

Swagger/OpenAPI - Документация API

🚀 Быстрый старт
Предварительные требования
Java 17 или выше

Maven 3.6+

Docker и Docker Compose

PostgreSQL (опционально, можно использовать в Docker)

Установка и запуск
Клонирование репозитория

bash
git clone <repository-url>
cd bank-cards-system
Запуск с Docker

bash
# Запуск всей инфраструктуры
docker-compose up -d

# Или только базы данных
docker-compose up postgres -d
Запуск приложения

bash
# Сборка и запуск
mvn clean package
java -jar target/bank-cards-*.jar

# Или для разработки
mvn spring-boot:run
Доступ к приложению

text
API: http://localhost:8080/api
Документация: http://localhost:8080/api/swagger-ui.html
База данных: localhost:5432
🔧 Конфигурация
Основные настройки
yaml
server:
port: 8080
servlet:
context-path: /api

spring:
datasource:
url: jdbc:postgresql://localhost:5432/Bank_DB
username: postgres
password: 1234

jwt:
secret: ваш-секретный-ключ
expiration: 86400000
Профили
dev - Разработка с H2 базой

test - Тестирование

prod - Продакшен конфигурация

🧪 Тестирование
bash
# Все тесты
mvn test

# Unit тесты
mvn test -Dtest="*UnitTest"

# Интеграционные тесты
mvn test -Dtest="*IT"

# С покрытием кода
mvn jacoco:report
📚 API Документация
После запуска приложения документация доступна по адресу:

Swagger UI: http://localhost:8080/api/swagger-ui.html

OpenAPI спецификация: http://localhost:8080/api/v3/api-docs

🐳 Docker
Сборка образа
bash
docker build -t bank-cards-app .
Запуск с Docker Compose
bash
# Полный стек
docker-compose up -d

# Только определенные сервисы
docker-compose up postgres app -d
🔒 Безопасность
Роли и разрешения
USER - Базовые операции со своими картами

ADMIN - Полный доступ ко всем функциям

Защита данных
Маскирование номеров карт

Шифрование паролей

Валидация входных данных

Защита от основных уязвимостей

📊 Мониторинг
Логирование
Структурированные логи в JSON формате

Раздельные логи для безопасности, бизнес-логики, запросов к БД

Health checks
text
GET /api/actuator/health
🤝 Разработка
Code style
Google Java Style Guide

Lombok для сокращения boilerplate кода

Единый стиль именования

Коммиты
Conventional Commits

Ясные описания изменений

Ветвление
main - стабильная версия

develop - разработка

feature/* - новые функции

hotfix/* - срочные исправления

🐛 Отладка и логи
Уровни логирования
yaml
logging:
level:
com.example.bankcards: DEBUG
org.springframework.security: DEBUG
org.hibernate.SQL: DEBUG