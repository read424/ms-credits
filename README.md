# MS-Credits Microservice

## Descripción

**MS-Credits** es el microservicio responsable de gestionar el ciclo de vida completo de los créditos bancarios en la plataforma de banking. Implementa una arquitectura hexagonal con Domain Driven Design (DDD) y utiliza un enfoque completamente reactivo con Spring WebFlux.

### Responsabilidades Principales

- Creación y gestión de créditos bancarios
- Cálculo de cuotas y planes de pago
- Validación de solicitudes de crédito
- Procesamiento de pagos y cobros
- Publicación de eventos de crédito a Kafka
- Almacenamiento de historial de créditos en MongoDB
- Caching de información de créditos con Redis
- Exposición de API REST para gestión de créditos

## Stack Tecnológico

- **Java 17** con Spring Boot 3.5.x
- **Spring WebFlux** - Programación reactiva no bloqueante
- **Spring Cloud** - Service discovery (Eureka), configuración centralizada
- **MongoDB** - Base de datos NoSQL para persistencia de créditos
- **Redis** - Caching distribuido para consultas frecuentes
- **Kafka** - Message broker para arquitectura event-driven
- **Resilience4j** - Circuit breaker, retry, rate limiter y time limiter
- **OpenTelemetry** - Observabilidad y trazabilidad distribuida

## Requisitos Previos

- Docker y Docker Compose
- Maven 3.9+
- Java 17 JDK
- Red Docker externa `bootcamp-network`

## Levantamiento Local

### 1. Crear la red Docker (si no existe)

```bash
docker network create bootcamp-network
```

### 2. Levantar infraestructura (MongoDB, Redis)

```bash
docker compose up -d mongodb redis
```

### 3. Iniciar el microservicio

```bash
docker compose up -d ms-credits
```

## Configuración

### Variables de Entorno

- `SPRING_PROFILES_ACTIVE: prod`
- `SPRING_CONFIG_IMPORT: configserver:http://service-config-server:8888`
- `EUREKA_CLIENT_SERVICE_URL_DEFAULT_ZONE: http://service-eureka:8761/eureka/`
- `SPRING_REDIS_HOST: redis-credits`
- `SPRING_REDIS_PORT: 6381`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka_broker:9092`

### MongoDB (desde Config Server)

- Host: `mongodb-credits`
- Database: `ms_credits_prod`
- Usuario: `admin`
- Contraseña: Encriptada

## Endpoints

```bash
# Health Check
curl http://localhost:8083/actuator/health

# Métricas Prometheus
curl http://localhost:8083/actuator/prometheus
```

## Puertos

- **8083** - API REST
- **27019** - MongoDB (container)
- **6381** - Redis (container)

## Service Discovery

Registrado en Eureka como: `ms-credits`

## Desenvolvimento

```bash
# Compilar
mvn clean package -DskipTests

# Buildear imagen
docker build -t read424/ms-credits:latest .
docker tag read424/ms-credits:latest read424/ms-credits:v1.0.0

# Levantar
docker compose up -d ms-credits

# Logs
docker logs -f ms-credits
```

## Kafka Topics

**Producidos:**
- `credit.created`
- `credit.payment.completed`
- `credit.charge.completed`

## Resilience Patterns

- Circuit Breaker (50% failure threshold)
- Retry (3-5 intentos)
- Rate Limiter (50-100 calls/min)
- Time Limiter (2-5 segundos)

## References

- [Spring WebFlux](https://spring.io/projects/spring-webflux)
- [Resilience4j](https://resilience4j.readme.io/)
- [MongoDB Reactive](https://spring.io/projects/spring-data-mongodb)
