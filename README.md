# customer-service

Microservicio encargado de la gestion de clientes. Permite crear, consultar,
actualizar y eliminar clientes, ademas de exponer un resumen usado por otros
servicios.

## Tecnologias

- Java 17
- Spring Boot 3.2.4
- Spring WebFlux
- Spring Data MongoDB Reactive
- Spring Data Redis Reactive
- Spring Kafka
- RxJava 3
- Reactor Adapter
- Spring Cloud Config
- Eureka Client
- Springdoc OpenAPI
- Maven
- JUnit 5
- JaCoCo
- Spotless
- Checkstyle

## Puerto

```text
http://localhost:8081
```

## OpenAPI

```text
http://localhost:8081/swagger-ui.html
http://localhost:8081/v3/api-docs
```

## Levantar sin Docker

Requisitos locales:

- Java 17
- Maven
- MongoDB en `localhost:27017`
- Redis en `localhost:6379`
- Kafka en `localhost:9092`
- Config Server opcional en `localhost:8888`
- Eureka en `localhost:8761`

```powershell
cd .\customer-service
mvn spring-boot:run
```

## Levantar con Docker

Primero levantar la infraestructura desde el repositorio o ruta de infraestructura:

```text
https://github.com/fernandosanchosamata/infra
```

```powershell
cd .\infra
docker compose up -d
```

Este proyecto aun no incluye `Dockerfile`. Cuando se agregue, debe conectarse a
MongoDB, Redis, Kafka, Config Server y Eureka usando nombres de servicio de Docker
Compose.

## Tests

```powershell
cd .\customer-service
mvn test
```

Si luego se agregan tests de integracion:

```powershell
mvn verify
```

## Formato y Checkstyle

```powershell
mvn spotless:apply
mvn checkstyle:check
```

## JaCoCo

```powershell
mvn test jacoco:report
```

Reporte:

```text
target/site/jacoco/index.html
```

## MongoDB

Base de datos:

```text
ntt_customer
```

Coleccion principal:

```text
customers
```

Consulta:

```powershell
mongosh
use ntt_customer
show collections
db.customers.find().pretty()
```

Se usa database per service logico: cada microservicio mantiene sus datos en una
base MongoDB independiente, aunque en desarrollo compartan el mismo servidor Mongo.

