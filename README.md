# Hotel Booking System

A production-ready, full-stack hotel booking platform built with **Spring Boot 3** (Java 21) and **React 18**.

> **Module 1 Status:** Project skeleton only — no business logic, entities, or APIs yet.

## Tech Stack

| Layer | Technologies |
|-------|-------------|
| Backend | Java 21, Spring Boot 3.3, Spring Data JPA, Spring Security, JWT, MySQL 8, Maven, Lombok, MapStruct, Swagger |
| Frontend | React 18, Vite, Material UI, React Router, Axios |
| DevOps | Docker, Docker Compose |

## Project Structure

```
hotel-booking/
├── backend/          # Spring Boot REST API
├── frontend/         # React SPA
├── docker/           # Dockerfiles & Nginx config
├── docs/             # Architecture documentation
└── docker-compose.yml
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed architecture.

## Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Node.js 20+
- MySQL 8 (or use Docker Compose)
- Docker & Docker Compose (optional)

## Quick Start (Local Development)

### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

API base URL: `http://localhost:8080/api`  
Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### 2. Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

App URL: `http://localhost:5173`

### 3. Full Stack with Docker

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

This project is built incrementally:

1. **Module 1** — Project structure *(current)*
2. **Module 2** — Database design & JPA entities
3. **Module 3** — Repositories & services
4. **Module 4** — REST APIs & DTOs
5. **Module 5** — JWT authentication
6. **Module 6** — Booking business logic
7. **Module 7** — React UI
8. **Module 8** — Production deployment

## License

Apache 2.0
