# Hotel Booking System

<<<<<<< HEAD
A production-ready, full-stack hotel booking platform built with **Spring Boot 3** (Java 21) and **React 18**.

> **Module 2 Status:** Persistence layer complete — entities, repositories, and Flyway SQL migrations. No services, controllers, or APIs yet.

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
=======
A production-oriented full-stack hotel booking platform: **Spring Boot 3 (Java 21)** + **React 18 (Vite / MUI)**, with Docker, Nginx, and GitHub Actions for deployment.

> **Module 15 Status:** Production Deployment & DevOps complete — Docker multi-stage images, Compose (dev/prod), Nginx reverse proxy, CI/CD workflows, Postman, and ops docs.  
> Guide: [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)

---

## Features

- JWT auth (CUSTOMER / ADMIN), guest & room management  
- Booking engine with availability, multi-room `roomIds[]`  
- Payments (Razorpay-style mock/live), invoices, webhooks  
- Email notification pipeline (templates / outbox-ready)  
- Admin reports & React admin dashboard  
- Public website + booking wizard UI  
- Automated tests (JUnit, Vitest, Playwright e2e)  
- **Production packaging:** Docker + Nginx + Compose + CI  

---

## Architecture (runtime)

```text
Browser → Nginx (SPA + /api proxy) → Spring Boot → MySQL
```

Deploy path:

```text
Developer → GitHub → GitHub Actions → Docker images → Compose host → Nginx → API → DB
```

Details: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) · [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)

---

## Technology Stack

| Layer | Technologies |
|-------|-------------|
| Backend | Java 21, Spring Boot 3.x, JPA, Security, JWT, MySQL 8, Maven |
| Frontend | React 18, Vite, MUI, React Router, Axios, RHF, Zod, Recharts |
| DevOps | Docker, Compose, Nginx, GitHub Actions |

---

## Prerequisites

- JDK 21, Maven 3.9+  
- Node 20+, npm  
- MySQL 8 **or** Docker  
- Docker Compose v2 (recommended)

---

## Installation & Local Setup

Full steps: **[docs/INSTALLATION.md](docs/INSTALLATION.md)**

### Backend
>>>>>>> feature/module-1-foundation-practice

```bash
cd backend
mvn spring-boot:run
```

<<<<<<< HEAD
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
=======
- API: `http://localhost:8080/api`  
- Swagger: `http://localhost:8080/api/swagger-ui.html` — [docs/SWAGGER.md](docs/SWAGGER.md)

### Frontend

```bash
cd frontend
npm ci
npm run dev
```

App: `http://localhost:5173` (Vite proxies `/api` → `:8080`)

### Docker (full stack)
>>>>>>> feature/module-1-foundation-practice

```bash
cp .env.example .env
docker compose up --build
```

<<<<<<< HEAD
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
| 1 | Project structure | Done |
| 2 | Database design & JPA entities | Done *(current)* |
| 3 | Service layer & business logic |  Next |
| 4 | REST APIs & DTOs | ⬜ |
| 5 | JWT authentication | ⬜ |
| 6 | Booking business logic | ⬜ |
| 7 | React UI | ⬜ |
| 8 | Production deployment | ⬜ |

## License

Apache 2.0
=======
- Site: `http://localhost:80`  
- API: `http://localhost:8080/api`

Production-shaped (profile `prod`, `/api/v1`):

```bash
./scripts/deploy.sh prod
```

---

## Environment Variables

See **[docs/ENVIRONMENT.md](docs/ENVIRONMENT.md)** and `.env.example`.  
Never commit real secrets.

---

## Database Setup

`ddl-auto: validate` — apply SQL under `backend/src/main/resources/db/migration/` before start.  
Compose can auto-load `V1` (+ `V2` seed) on first MySQL volume.  
Guide: [docs/DATABASE.md](docs/DATABASE.md)

---

## Swagger & Postman

- Swagger guide: [docs/SWAGGER.md](docs/SWAGGER.md)  
- Postman: import `postman/Hotel_Booking_API.postman_collection.json` + local environment  

---

## Screenshots

Place captures in `docs/screenshots/` — see [docs/screenshots/README.md](docs/screenshots/README.md).

| | |
|--|--|
| Home | `docs/screenshots/01-home.png` |
| Rooms | `docs/screenshots/02-rooms.png` |
| Booking | `docs/screenshots/03-booking-wizard.png` |
| Admin | `docs/screenshots/05-admin-dashboard.png` |

---

## Folder Structure

```text
hotel-booking/
├── backend/                 # Spring Boot API
├── frontend/                # React SPA (Vite)
├── docker/                  # Dockerfiles + Nginx configs
├── nginx/                   # Pointers to docker Nginx configs
├── .github/workflows/       # CI + CD template
├── scripts/                 # deploy.sh, backup-mysql.sh
├── docs/                    # Learning + ops guides
├── postman/                 # API collection
├── docker-compose.yml       # Base / local stack
├── docker-compose.dev.yml   # Dev overrides
├── docker-compose.prod.yml  # Production-shaped stack
└── .env.example             # Env template
```

Learning path: [docs/MODULES.md](docs/MODULES.md)

| Doc | Topic |
|-----|-------|
| [DEPLOYMENT.md](docs/DEPLOYMENT.md) | Module 15 — Docker, Nginx, CI/CD |
| [INSTALLATION.md](docs/INSTALLATION.md) | Install options |
| [ENVIRONMENT.md](docs/ENVIRONMENT.md) | Env vars & secrets |
| [PRODUCTION_CHECKLIST.md](docs/PRODUCTION_CHECKLIST.md) | Go-live checklist |
| [RELEASE_NOTES.md](docs/RELEASE_NOTES.md) | Module 15 notes |
| [TESTING.md](docs/TESTING.md) | Module 14 testing |
| … | Modules 2–13 guides linked in older sections |

---

## Deployment Guide

1. Configure `.env` secrets  
2. `docker compose -f docker-compose.prod.yml up --build -d`  
3. Confirm health: `docker compose -f docker-compose.prod.yml ps`  
4. Backup: `./scripts/backup-mysql.sh`  
5. CI: push → GitHub Actions `CI` workflow  
6. CD: enable template jobs in `.github/workflows/cd.yml` after registry secrets  

Troubleshooting: [INSTALLATION.md](docs/INSTALLATION.md) · [DEPLOYMENT.md](docs/DEPLOYMENT.md)

---

## Future Enhancements

- Spring Boot Actuator + Prometheus metrics  
- TLS at Nginx or cloud LB  
- Kubernetes manifests / Helm  
- Managed MySQL + automated backups  
- Disable or protect Swagger in public prod  

---

## Contributing

1. Branch from `main` / feature branch  
2. Keep `pom.xml` / core business changes intentional and reviewed  
3. Ensure `mvn verify` and `npm test` pass  
4. Path-scope commits by module when possible  
5. Never commit `.env` or secrets  

---

## License

Proprietary / training project — update this section if you open-source the repo.

---

## Module status

| Module | Focus | Status |
|--------|-------|--------|
| 1–9 | Backend core → reports | Done |
| 10–13 | React auth, public site, booking UI, admin | Done |
| 14 | Testing | Done |
| 15 | Production Deployment & DevOps | Done *(current)* |
>>>>>>> feature/module-1-foundation-practice
