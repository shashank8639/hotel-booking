# Hotel Booking System

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

```bash
cd backend
mvn spring-boot:run
```

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

```bash
cp .env.example .env
docker compose up --build
```

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
