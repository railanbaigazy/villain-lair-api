# Villain Lair API

REST backend for a command-based **hero vs villain strategy game**. Heroes raid villain lairs, villains fortify their defences, and every important action is recorded as a Kafka game event.

---

## Tech Stack

| Layer       | Technology                              |
| ----------- |-----------------------------------------|
| Runtime     | Java 17, Spring Boot 4                  |
| Web         | Spring MVC (spring-boot-starter-webmvc) |
| Persistence | PostgreSQL 16, Spring Data JPA, Flyway  |
| Messaging   | Apache Kafka (KRaft)                    |
| Security    | Spring Security, JWT (JJWT 0.12)        |
| Docs        | SpringDoc OpenAPI 3 (Swagger UI)        |
| Build       | Maven                                   |
| Containers  | Docker Compose                          |

---

## Running with Docker Compose

Starts PostgreSQL, Kafka (KRaft), Kafka UI, and the application.

```bash
docker compose up --build
```

| Service    | URL                                   |
| ---------- | ------------------------------------- |
| API        | http://localhost:8080                 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Kafka UI   | http://localhost:8090                 |
| PostgreSQL | localhost:5432                        |

Stop and remove volumes:

```bash
docker compose down -v
```

---

## Running Locally

**Prerequisites:** Java 17, Maven, PostgreSQL running on localhost:5432, Kafka running on localhost:9092.

Create the database:

```sql
CREATE DATABASE villain_lair;
CREATE USER villain WITH PASSWORD 'villain';
GRANT ALL PRIVILEGES ON DATABASE villain_lair TO villain;
```

Run the app:

```bash
./mvnw spring-boot:run
```

---

## Environment Variables

| Variable                         | Default                                         | Description                       |
| -------------------------------- | ----------------------------------------------- | --------------------------------- |
| `SPRING_DATASOURCE_URL`          | `jdbc:postgresql://localhost:5432/villain_lair` | JDBC URL                          |
| `SPRING_DATASOURCE_USERNAME`     | `villain`                                       | DB user                           |
| `SPRING_DATASOURCE_PASSWORD`     | `villain`                                       | DB password                       |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092`                                | Kafka brokers                     |
| `APP_JWT_SECRET`                 | `change-me-...`                                 | JWT signing secret (min 32 chars) |
| `APP_JWT_EXPIRATION_MINUTES`     | `60`                                            | Token lifetime in minutes         |

---

## Roles

| Role      | Description                                                                                            |
| --------- | ------------------------------------------------------------------------------------------------------ |
| `HERO`    | Buys weapons and potions, equips weapons, browses lairs, attacks villain lairs, heals with consumables |
| `VILLAIN` | Manages their lair, buys defences, upgrades/repairs lair, views attack history                         |

---

## Main Endpoints

### Auth

| Method | Path                    | Access | Description                 |
| ------ | ----------------------- | ------ | --------------------------- |
| POST   | `/api/v1/auth/register` | Public | Register as HERO or VILLAIN |
| POST   | `/api/v1/auth/login`    | Public | Login, receive JWT          |

### Hero

| Method | Path                                               | Access | Description                      |
| ------ | -------------------------------------------------- | ------ | -------------------------------- |
| GET    | `/api/v1/heroes/me`                                | HERO   | Hero profile and equipped weapon |
| GET    | `/api/v1/heroes/me/inventory`                      | HERO   | All weapon inventory items       |
| POST   | `/api/v1/heroes/me/inventory/{id}/equip`           | HERO   | Equip a weapon                   |
| GET    | `/api/v1/heroes/me/inventory/consumables`          | HERO   | List consumable items (potions)  |
| POST   | `/api/v1/heroes/me/inventory/consumables/{id}/use` | HERO   | Use a consumable to heal         |
| GET    | `/api/v1/heroes/me/attacks`                        | HERO   | Battle history                   |

### Villain

| Method | Path                                        | Access  | Description                          |
| ------ | ------------------------------------------- | ------- | ------------------------------------ |
| GET    | `/api/v1/villains/me`                       | VILLAIN | Villain profile                      |
| GET    | `/api/v1/villains/me/lair`                  | VILLAIN | Lair details                         |
| PATCH  | `/api/v1/villains/me/lair`                  | VILLAIN | Rename lair                          |
| POST   | `/api/v1/villains/me/lair/upgrade-security` | VILLAIN | Upgrade security level (costs coins) |
| POST   | `/api/v1/villains/me/lair/repair`           | VILLAIN | Repair lair health (costs coins)     |
| GET    | `/api/v1/villains/me/lair/attacks`          | VILLAIN | Incoming attack history              |

### Lairs (public scouting)

| Method | Path                 | Access        | Description                                       |
| ------ | -------------------- | ------------- | ------------------------------------------------- |
| GET    | `/api/v1/lairs`      | Authenticated | List all lairs                                    |
| GET    | `/api/v1/lairs/{id}` | Authenticated | Lair detail (defense units visible to owner only) |

### Shop

| Method | Path                          | Access        | Description                   |
| ------ | ----------------------------- | ------------- | ----------------------------- |
| GET    | `/api/v1/shop/items`          | Authenticated | Items available for your role |
| POST   | `/api/v1/shop/items/{id}/buy` | Authenticated | Purchase item                 |

### Battle

| Method | Path                     | Access        | Description                       |
| ------ | ------------------------ | ------------- | --------------------------------- |
| POST   | `/api/v1/heroes/attacks` | HERO          | Attack a lair                     |
| GET    | `/api/v1/battles/{id}`   | Authenticated | Battle detail (participants only) |

### Events

| Method | Path                                       | Access        | Description                      |
| ------ | ------------------------------------------ | ------------- | -------------------------------- |
| GET    | `/api/v1/events`                           | Authenticated | Recent game event log (last 100) |
| GET    | `/api/v1/events?type=AttackCompletedEvent` | Authenticated | Filtered by event type           |

---

## Kafka Topics

| Topic                         | Event Type                  | Published When                 |
| ----------------------------- | --------------------------- | ------------------------------ |
| `game.user.registered`        | `UserRegisteredEvent`       | User registers                 |
| `game.item.purchased`         | `ItemPurchasedEvent`        | Item bought from shop          |
| `game.weapon.equipped`        | `WeaponEquippedEvent`       | Hero equips a weapon           |
| `game.lair.security-upgraded` | `LairSecurityUpgradedEvent` | Villain upgrades lair security |
| `game.lair.repaired`          | `LairRepairedEvent`         | Villain repairs lair           |
| `game.attack.completed`       | `AttackCompletedEvent`      | Battle finishes                |

Events are consumed by the internal `GameEventConsumer` which:

- Stores every event in `game_event_logs` table (queryable via `GET /api/v1/events`)
- Updates `user_stats` (attacks won/lost, defenses won/lost) when an `AttackCompletedEvent` is consumed

---

## Tests

```bash
./mvnw test
```
