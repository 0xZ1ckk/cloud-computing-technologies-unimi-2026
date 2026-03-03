# Smart IT Ticketing System - API Gateway

Spring Boot microservices with a Spring Cloud Gateway entry point.

## Stack

- Java 21
- Maven
- Spring Boot 3.2.x
- Spring Cloud Gateway
- PostgreSQL

## Services and Ports

- API Gateway: `8080`
- User Service: `8081`
- Ticket Service: `8082` (run multiple instances)
- Notification Service: `8083`

## Local PostgreSQL

Create a database named `ticketing` and run Postgres locally.

Default credentials (adjust in each `application.yml` if needed):

- user: `postgres`
- pass: `postgres`
- url: `jdbc:postgresql://localhost:5432/ticketing`

## Build

```bash
mvn -q -DskipTests package
```

## Run Services

```bash
mvn -q -pl user-service spring-boot:run
```

```bash
mvn -q -pl notification-service spring-boot:run
```

```bash
mvn -q -pl ticket-service spring-boot:run -Dspring-boot.run.arguments="--server.port=8082 --app.instance-id=ticket-1"
```

```bash
mvn -q -pl ticket-service spring-boot:run -Dspring-boot.run.arguments="--server.port=8084 --app.instance-id=ticket-2"
```

```bash
mvn -q -pl api-gateway spring-boot:run
```

## API Overview

Gateway routes:

- `/auth/**` -> User Service
- `/tickets/**` -> Ticket Service (load balanced)
- `/notifications/**` -> Notification Service

### Register and Login

```bash
curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password"}'
```

```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password"}'
```

### Create Ticket

```bash
curl -s -X POST http://localhost:8080/tickets \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Email down","description":"Cannot send emails"}'
```

### List Tickets

```bash
curl -s http://localhost:8080/tickets \
  -H "Authorization: Bearer <TOKEN>"
```

### Update Ticket Status (ADMIN only)

```bash
curl -s -X PUT http://localhost:8080/tickets/1 \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"status":"IN_PROGRESS"}'
```

## Notes

- JWT validation, role checks, and rate limiting are enforced at the gateway.
- Ticket creation is limited to 5 requests per minute per user by default.
- Run multiple ticket-service instances to see round-robin load balancing.
- To create an admin, update the `users.role` column to `ADMIN` in the database.
