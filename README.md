# ms-credits - Microservicio de Créditos

Microservicio reactivo de gestión de créditos basado en Spring Boot 3.5.15, WebFlux, MongoDB y Kafka KRaft.

## 📋 Requisitos Previos

- **Docker** >= 20.10
- **Docker Compose** >= 2.0
- **Java 17** (para desarrollo local)
- **Maven 3.9+** (para desarrollo local)
- Red Docker `bootcamp-network` creada

## 🚀 Guía Rápida de Inicio

### 1. Crear la Red Docker (Una sola vez)

```bash
docker network create bootcamp-network
```

Verificar que la red fue creada:
```bash
docker network ls | grep bootcamp-network
```

### 2. Navegar al Directorio del Proyecto

```bash
cd /home/developer01/Descargas/bootcamp-nttdata/ms-credits
```

### 3. Construir la Imagen Docker

```bash
docker build -t ms-credits:latest .
```

O dejar que docker-compose la construya automáticamente en el siguiente paso.

### 4. Levantar los Servicios

```bash
docker-compose up -d
```

Esto iniciará en segundo plano:
- ✅ **MongoDB** (puerto 27017)
- ✅ **Redis** (puerto 6381)
- ✅ **Kafka** (puerto 9092)
- ✅ **ms-credits** (puerto 8083)

### 5. Verificar que los Servicios Estén Activos

```bash
docker-compose ps
```

Deberías ver todos los servicios en estado `Up`.

### 6. Validar la Salud del Microservicio

```bash
curl http://localhost:8083/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

### 7. Acceder a la API

- **Swagger/OpenAPI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus
- **Info**: http://localhost:8080/actuator/info

## 📚 Guías Detalladas

### Ver Logs en Tiempo Real

```bash
# Todos los servicios
docker-compose logs -f

# Solo ms-credits
docker-compose logs -f ms-credits

# Solo MongoDB
docker-compose logs -f mongodb

# Últimas 50 líneas
docker-compose logs --tail=50 ms-credits
```

### Detener los Servicios

```bash
# Detener sin eliminar volúmenes (datos persisten)
docker-compose stop

# Reanudar servicios detenidos
docker-compose start

# Detener y eliminar todo (incluyendo volúmenes)
docker-compose down -v
```

### Reconstruir la Imagen

Si realizaste cambios en el código:

```bash
docker-compose down
docker build -t ms-credits:latest .
docker-compose up -d
```

O simplemente:

```bash
docker-compose up -d --build
```

## 🗄️ Acceder a las Bases de Datos

### MongoDB

```bash
# Conectarse a MongoDB
docker exec -it mongodb-credits mongosh -u admin -p bootcamp_credits_prod_2024

# Una vez conectado:
show databases
use ms_credits_prod
db.getCollectionNames()
```

### Redis

```bash
# Conectarse a Redis
docker exec -it redis-credits redis-cli -a eYVX7EwVmmxKPCDmwMtyKVge8oLd2t81

# Ver claves
KEYS *
```

## 🎯 Kafka

### Listar Tópicos Creados

```bash
docker exec kafka_broker kafka-topics.sh --bootstrap-server kafka_broker:9094 --list
```

### Crear un Tópico Manualmente

```bash
docker exec kafka_broker kafka-topics.sh --bootstrap-server kafka_broker:9094 \
  --create --topic test-topic --partitions 1 --replication-factor 1
```

### Consumir Mensajes de un Tópico

```bash
docker exec kafka_broker kafka-console-consumer.sh \
  --bootstrap-server kafka_broker:9094 \
  --topic credit.created \
  --from-beginning
```

## 🔧 Configuración

### Archivo de Configuración Principal

Ubicación: `src/main/resources/application-prod.yaml`

**Nota**: La configuración de MongoDB (host, puerto, credenciales) se obtiene del **Cloud Config Server** con valores encriptados. El archivo local solo contiene configuración básica.

Principales valores en archivo local:
- **Puerto del Microservicio**: `8083`
- **Auto-index Creation MongoDB**: `true`
- **Kafka Bootstrap**: Obtenido de variable de entorno
- **Redis**: Obtenido de variables de entorno

Valores sensibles (desde Cloud Config Server):
- **MongoDB Host, Port, Database, Credenciales**: Centralizadas y encriptadas
- **Redis Password**: Obtenido de variables de entorno

### Variables de Entorno (docker-compose.yml)

Para modificar la memoria JVM:
```yaml
environment:
  JAVA_OPTS: -Xmx512m -Xms256m
```

Para cambiar el perfil activo:
```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
```

## 🐛 Troubleshooting

### El contenedor no inicia

**Síntoma**: `docker-compose up` muestra errores

**Solución**:
```bash
# Ver logs detallados
docker-compose logs ms-credits

# Verificar que la red existe
docker network ls | grep bootcamp-network

# Reconstruir sin cachés
docker-compose up --build --no-cache
```

### Puerto ya está en uso

**Síntoma**: `Error: Port 8083 is already allocated`

**Solución**:
```bash
# Encontrar qué proceso usa el puerto
lsof -i :8080

# Cambiar puerto en docker-compose.yml
# ports:
#   - "8081:8083"  # cambiar 8081 por otro puerto
```

### MongoDB no inicia

**Síntoma**: MongoDB en estado `Exited`

**Solución**:
```bash
# Eliminar volúmenes de MongoDB
docker volume rm bootcamp-nttdata_ms-credits_mongodb_credits_data

# Reiniciar
docker-compose up -d mongodb
```

### Redis no responde

**Síntoma**: Redis no se conecta

**Solución**:
```bash
# Verificar salud de Redis
docker exec redis-credits redis-cli -a eYVX7EwVmmxKPCDmwMtyKVge8oLd2t81 ping

# Debería responder: PONG
```

### Kafka no está saludable

**Síntoma**: Kafka no aparece en logs o con estado `Exited`

**Solución**:
```bash
# Ver logs detallados de Kafka
docker-compose logs kafka

# Verificar el volumen
docker volume ls | grep kafka_data

# Reiniciar Kafka
docker-compose restart kafka
```

## 👨‍💻 Desarrollo Local (Sin Docker)

Para desarrollar sin contenedores:

### Requisitos Adicionales
- MongoDB corriendo localmente en puerto 27017
- Redis corriendo localmente en puerto 6379
- Kafka corriendo localmente en puerto 9092

### Iniciar la Aplicación

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

O si tienes Maven instalado globalmente:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

## 📊 Monitoreo

### Prometheus Metrics

```bash
curl http://localhost:8080/actuator/prometheus | grep -i credit
```

### Health Detailed

```bash
curl http://localhost:8080/actuator/health
```

### Circuit Breaker Status

```bash
curl http://localhost:8080/actuator/circuitbreakers
```

## 🧹 Limpiar Recursos

### Eliminar todo (containers, volúmenes, networks no usadas)

```bash
docker-compose down -v
docker volume prune
docker network prune
```

### Solo eliminar contenedores (mantener volúmenes)

```bash
docker-compose down
```

## 📝 Archivos Importantes

- **Dockerfile**: Configuración para construir la imagen
- **.dockerignore**: Archivos a excluir del build
- **docker-compose.yml**: Orquestación de servicios
- **application-prod.yaml**: Configuración para ambiente de producción
- **DOCKER-GUIDE.md**: Guía detallada de Docker

## 📖 Documentación Adicional

Para más información sobre Docker, consulta [DOCKER-GUIDE.md](./DOCKER-GUIDE.md)

## 🤝 Integración con Otros Microservicios

Todos los microservicios deben estar en la red `bootcamp-network`:

```bash
# Desde otro contenedor, acceder a ms-credits
curl http://ms-credits:8083/actuator/health
```

## 💡 Tips Útiles

### Ejecutar comandos dentro de un contenedor

```bash
docker exec -it ms-credits sh
```

### Ver tamaño de volúmenes

```bash
docker volume ls -q | xargs -I {} sh -c 'echo {} && docker volume inspect {} | grep Mountpoint'
```

### Exportar logs a archivo

```bash
docker-compose logs > logs.txt
```

### Monitorear recursos en tiempo real

```bash
docker stats
```

## ✅ Checklist de Verificación

- [ ] Red `bootcamp-network` creada
- [ ] Imagen `ms-credits:latest` construida
- [ ] Todos los servicios en estado `Up`
- [ ] MongoDB responde en puerto 27017
- [ ] Redis responde en puerto 6381
- [ ] Kafka responde en puerto 9092
- [ ] ms-credits responde en puerto 8083
- [ ] Health check retorna status UP
- [ ] Swagger accesible en http://localhost:8080/swagger-ui.html

## 📞 Soporte

Para reportar problemas, revisar:
1. Los logs: `docker-compose logs`
2. El estado de servicios: `docker-compose ps`
3. La salud de cada servicio individuamente

---

**Última actualización**: 2026-07-01
