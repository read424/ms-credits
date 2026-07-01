# Docker Guide - ms-credits

## Prerequisites

- Docker >= 20.10
- Docker Compose >= 2.0
- La red `bootcamp-network` debe existir en el host

## Network Setup

Si la red `bootcamp-network` no existe, créala:

```bash
docker network create bootcamp-network
```

## Building the Image

Para construir la imagen de Docker del microservicio:

```bash
docker build -t ms-credits:latest .
```

O dejar que `docker-compose` la construya automáticamente.

## Running with Docker Compose

### Iniciar todos los servicios

```bash
docker-compose up -d
```

Esto iniciará:
- **MongoDB** (puerto 27017)
- **Redis** (puerto 6381)
- **Kafka KRaft** (puerto 9092)
- **ms-credits** (puerto 8080)

### Ver logs

```bash
# Logs de todos los servicios
docker-compose logs -f

# Logs de un servicio específico
docker-compose logs -f ms-credits
docker-compose logs -f mongodb
docker-compose logs -f redis
docker-compose logs -f kafka
```

### Detener los servicios

```bash
docker-compose down
```

### Detener y eliminar volúmenes (limpia datos)

```bash
docker-compose down -v
```

## Service Configuration

### MongoDB
- **Host**: mongodb-credits
- **Puerto**: 27017
- **Usuario**: admin
- **Contraseña**: bootcamp_credits_prod_2024
- **Base de datos**: ms_credits_prod

### Redis
- **Host**: redis-credits
- **Puerto**: 6379
- **Contraseña**: eYVX7EwVmmxKPCDmwMtyKVge8oLd2t81

### Kafka
- **Host**: kafka_broker
- **Puerto (INTERNAL)**: 9094
- **Puerto (EXTERNAL)**: 9092
- **Broker ID**: 1
- **Cluster ID**: r4zt_wrqTRuT7W2NJsB_GA

### ms-credits
- **Puerto**: 8080
- **Perfil Activo**: prod
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus

## Health Checks

Verificar que los servicios están saludables:

```bash
# Todos los servicios
docker-compose ps

# Health check específico del microservicio
curl http://localhost:8080/actuator/health

# Metrics de Prometheus
curl http://localhost:8080/actuator/prometheus
```

## Kafka Operations

### Conectarse a Kafka

```bash
docker exec -it kafka_broker bash
```

### Listar tópicos

```bash
docker exec kafka_broker kafka-topics.sh --bootstrap-server kafka_broker:9094 --list
```

### Crear un tópico

```bash
docker exec kafka_broker kafka-topics.sh --bootstrap-server kafka_broker:9094 --create --topic test-topic --partitions 1 --replication-factor 1
```

### Consumir mensajes

```bash
docker exec kafka_broker kafka-console-consumer.sh --bootstrap-server kafka_broker:9094 --topic credit.created --from-beginning
```

## MongoDB Operations

### Conectarse a MongoDB

```bash
docker exec -it mongodb-credits mongosh -u admin -p bootcamp_credits_prod_2024
```

### Ver bases de datos

```javascript
show databases
```

### Cambiar a la base de datos de credits

```javascript
use ms_credits_prod
db.getCollectionNames()
```

## Redis Operations

### Conectarse a Redis

```bash
docker exec -it redis-credits redis-cli -a eYVX7EwVmmxKPCDmwMtyKVge8oLd2t81
```

### Ver claves

```bash
KEYS *
```

## Troubleshooting

### El microservicio no inicia

Verificar logs:
```bash
docker-compose logs ms-credits
```

Causas comunes:
- La red `bootcamp-network` no existe
- MongoDB/Redis no están saludables
- Puerto 8080 ya está en uso

### MongoDB no inicia

```bash
docker-compose logs mongodb
# Verificar que el volumen tiene permisos correctos
```

### Kafka no inicia

```bash
docker-compose logs kafka
# Verificar el volumen kafka_data
```

## Performance Tuning

### JVM Memory (ms-credits)

Editar en `docker-compose.yml`:
```yaml
environment:
  JAVA_OPTS: -Xmx1024m -Xms512m
```

### MongoDB Data Retention

En `docker-compose.yml`, si es necesario cambiar la retención:
```yaml
environment:
  MONGO_INITDB_DATABASE: ms_credits_prod
```

## Development vs Production

Este `docker-compose.yml` está configurado para ambiente de **producción (prod)**.

Para desarrollo local sin Docker, usar:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

## Integración con otros Microservicios

Para comunicar ms-credits con otros microservicios:

1. Asegurarse de que todos están en la red `bootcamp-network`
2. Usar los nombres de los contenedores como hostnames (ej: `http://ms-accounts:8082`)
3. Configurar Eureka en `EUREKA_CLIENT_SERVICE_URL_DEFAULT_ZONE`

## CI/CD

Para usar en pipelines de CI/CD:

```bash
# Build
docker build -t ms-credits:${VERSION} .

# Push a registry
docker tag ms-credits:${VERSION} my-registry/ms-credits:${VERSION}
docker push my-registry/ms-credits:${VERSION}

# Deploy
docker-compose -f docker-compose.yml up -d
```
