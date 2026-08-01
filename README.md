# Hotel Booking System

A production-ready, full-stack hotel booking platform built with **Spring Boot 3** (Java 21) and **React 18**.

> **Module 4 Status:** Guest Management complete — CRUD APIs, validation, pagination, search, and unit tests. Security/JWT infrastructure from Module 3 is in place.

## Tech Stack

| Layer | Technologies |
|-------|-------------|
| Backend | Java 21, Spring Boot 3.x, Spring Data JPA, Hibernate, Spring Security, JWT, MySQL 8, Flyway (SQL ready), Maven, Lombok, MapStruct, Swagger |
| Frontend | React 18, Vite, Material UI, React Router, Axios |
| DevOps | Docker, Docker Compose |

## Project Structure

```
hotel-booking/
├── backend/
│   └── src/main/
│       ├── java/com/hotelbooking/
│       │   ├── database/     # Domain enums (BookingStatus, RoomType, …)
│       │   ├── entity/       # JPA entities (Guest, Room, Booking, …)
│       │   ├── repository/   # Spring Data JPA repositories
│       │   └── migration/    # Flyway package (SQL in resources)
│       └── resources/db/migration/   # V1 schema, V2 seed data
├── frontend/         # React SPA
├── docker/           # Dockerfiles & Nginx config
├── docs/             # Architecture & database documentation
└── docker-compose.yml
```

- [docs/MODULES.md](docs/MODULES.md) — learning module index and progress
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — system architecture
- [docs/DATABASE.md](docs/DATABASE.md) — schema, ER diagram, migration guide
- [docs/SECURITY.md](docs/SECURITY.md) — JWT, RBAC, auth tables (Module 3)
- [docs/GUESTS.md](docs/GUESTS.md) — Guest Management APIs (Module 4)

## Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Node.js 20+
- MySQL 8 (or use Docker Compose)
- Docker & Docker Compose (optional)

## Quick Start (Local Development)

### 1. Database (required before backend against MySQL)

`application.yml` uses `ddl-auto: validate`, so tables must exist before startup:

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS hotel_booking;"
mysql -u root -p hotel_booking < backend/src/main/resources/db/migration/V1__create_tables.sql
mysql -u root -p hotel_booking < backend/src/main/resources/db/migration/V2__seed_sample_data.sql   # optional
```

See [docs/DATABASE.md](docs/DATABASE.md) for full details.

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

API base URL: `http://localhost:8080/api`  
Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### 3. Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

App URL: `http://localhost:5173`

### 4. Full Stack with Docker

```bash
cp .env.example .env
docker compose up --build
```

- Frontend: `http://localhost`
- Backend API: `http://localhost:8080/api`
- MySQL: `localhost:3306`

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `hotel_booking` | Database name |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | `root` | Database password |
| `JWT_SECRET` | (see yml) | JWT signing key — **change in production** |
| `SERVER_PORT` | `8080` | Backend port |

## Learning Modules

This project is built incrementally across 8 modules. See **[docs/MODULES.md](docs/MODULES.md)** for the full index, deliverables, and progress tracker.

| # | Module | Status |
|---|--------|--------|
| 1 | Project structure | ✅ |
| 2 | Database design & JPA entities | ✅ |
| 3 | Security & JWT authentication | ✅ |
| 4 | Guest Management (CRUD APIs) | ✅ *(current)* |
| 5 | Room Management | 🔜 Next |
| 6 | Booking business logic | ⬜ |
| 7 | React UI | ⬜ |
| 8 | Production deployment | ⬜ |

## License

Apache 2.0
