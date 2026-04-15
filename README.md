# villain-lair-api

Spring Boot REST API for a simple hero versus villain lair strategy game.

## Stack

- Java 17
- Spring Boot
- Maven
- Spring Web, Security, Data JPA, Validation
- PostgreSQL
- Flyway
- Lombok
- JJWT
- Kafka dependency included for later slices
- Swagger UI

## Run Locally

Start infrastructure:

```bash
docker compose up -d
```

That starts PostgreSQL, Kafka, and the API container.

For local development without the API container, start only dependencies:

```bash
docker compose up -d postgres kafka
```

Then run the app:

```bash
./mvnw spring-boot:run
```

Flyway runs automatically on startup. Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

## API Examples

Register a villain:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"doomlord","email":"doom@example.com","password":"password123","role":"VILLAIN","lairName":"Doom Keep"}'
```

Register a hero:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"brightblade","email":"hero@example.com","password":"password123","role":"HERO"}'
```

Login:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"hero@example.com","password":"password123"}'
```

List lairs:

```bash
curl http://localhost:8080/api/v1/lairs
```

Get the current hero:

```bash
curl http://localhost:8080/api/v1/heroes/me \
  -H "Authorization: Bearer $TOKEN"
```

Attack a lair:

```bash
curl -X POST http://localhost:8080/api/v1/heroes/attacks \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"targetLairId":1}'
```

## Security

- `/api/v1/auth/register` and `/api/v1/auth/login` are public.
- `GET /api/v1/lairs` is public.
- `/api/v1/heroes/**` requires a valid JWT with role `HERO`.
- Passwords are stored with BCrypt.
- JWT is stateless and sent as `Authorization: Bearer <token>`.
