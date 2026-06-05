# StayFinder — Hotel Booking System

Full-stack **multi-hotel** booking platform (Telangana catalog) built with **Spring Boot 3** (Java 21) and **React 18**.

> **Current status:** Module 16 — multi-hotel search, book, pay, JWT, admin/owner APIs, reports, Docker (portfolio-ready).

Public brand: **StayFinder**. Individual hotels (e.g. Grand Horizon Hyderabad) are catalog rows, not the whole product.

## Tech Stack

| Layer | Technologies |
|-------|-------------|
| Backend | Java 21, Spring Boot 3.x, Spring Data JPA, Hibernate, Spring Security + JWT, MySQL 8, Maven, Lombok, MapStruct, OpenAPI/Swagger |
| Frontend | React 18, Vite, Material UI, React Router, Axios, Playwright (E2E) |
| Payments / mail | Razorpay gateway (mock + live), transactional email outbox |
| DevOps | Docker, Docker Compose, GitHub Actions CI/CD samples |

## What you can do

- Search hotels by city / name / stars (Telangana seed data)
- Open hotel → list rooms → book with overlap protection (pessimistic room lock)
- Pay with **mock Razorpay** locally (`APP_RAZORPAY_MOCK_ENABLED=true`)
- Customer dashboard + **ADMIN** operations UI (`/admin`)
- Hotel owner submit / admin approve listing APIs

## Project Structure

```
hotel-booking/
├── backend/
│   └── src/main/
│       ├── java/com/hotelbooking/
│       │   ├── controller/   # REST (hotels, bookings, payments, admin, …)
│       │   ├── service/      # Business rules
│       │   ├── repository/   # Spring Data JPA
│       │   ├── entity/       # JPA entities (Hotel, Room, Booking, …)
│       │   ├── payment/      # Razorpay mock/live gateways
│       │   ├── notification/ # Email outbox + senders
│       │   └── security/     # JWT filter chain, RBAC
│       └── resources/db/migration/   # V1…V12 SQL (apply in order)
├── frontend/         # StayFinder SPA (Vite)
├── docker/           # Dockerfiles & Nginx
├── docs/             # Teaching guides per module
├── postman/          # API collection
└── docker-compose.yml
```

## Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Node.js 20+
- MySQL 8
- Docker & Docker Compose (optional)

## Quick Start (Local)

### 1. Database

Create the DB, then apply migrations **in order** (Flyway auto-run is not required if you apply SQL manually):

```bash
mysql -h 127.0.0.1 -u root -p -e "CREATE DATABASE IF NOT EXISTS hotel_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

cd backend/src/main/resources/db/migration
for f in V1__*.sql V2__*.sql V3__*.sql V4__*.sql V5__*.sql V6__*.sql V7__*.sql V8__*.sql V9__*.sql V10__*.sql V12__*.sql; do
  echo "Applying $f"
  mysql -h 127.0.0.1 -u root -p hotel_booking < "$f"
done
```

If `$` appears in your MySQL password, quote it: `-p'$YourPassword'`.

`V11__report_indexes.sql.example` is optional (indexes for reports).

See [docs/DATABASE.md](docs/DATABASE.md) and [docs/MULTI_HOTEL.md](docs/MULTI_HOTEL.md).

### 2. Backend

```bash
cd backend
# Prefer backend/.env for JWT_SECRET, DB_*, APP_RAZORPAY_MOCK_ENABLED=true, etc.
set -a && source .env && set +a   # if you keep secrets in .env
mvn spring-boot:run
```

- API: `http://localhost:8080/api`
- Swagger: `http://localhost:8080/api/swagger-ui.html`

JDBC URL should include `connectionCollation=utf8mb4_unicode_ci` (already in `application.yml`) so hotel search `LIKE` works on MySQL 8.

### 3. Frontend

```bash
cd frontend
cp .env.example .env   # VITE_API_BASE_URL=http://localhost:8080/api
npm install
npm run dev
```

App: `http://localhost:5173`

### 4. Docker (optional)

```bash
cp .env.example .env
docker compose up --build
```

## Useful roles

| Role | How |
|------|-----|
| `CUSTOMER` | Default on register / login |
| `ADMIN` | Promote in SQL: insert into `user_roles` joining `roles.name = 'ADMIN'`, then **re-login** |
| `HOTEL_OWNER` | Same pattern with role `HOTEL_OWNER` (Module 16) |

## Environment (common)

| Variable | Description |
|----------|-------------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | MySQL connection |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL credentials |
| `JWT_SECRET` | Signing key — **change in production** |
| `APP_RAZORPAY_MOCK_ENABLED` | `true` for local demo checkout |
| `APP_MAIL_ENABLED` | Local email / outbox behavior |
| `VITE_API_BASE_URL` | Frontend → API base (include `/api`) |

More detail: [docs/ENVIRONMENT.md](docs/ENVIRONMENT.md).

## Learning path

Start at **[docs/MODULES.md](docs/MODULES.md)** (Module 16 is current).

| Doc | Topic |
|-----|--------|
| [MULTI_HOTEL.md](docs/MULTI_HOTEL.md) | Multi-hotel platform (Module 16) |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Layers & packages |
| [DATABASE.md](docs/DATABASE.md) | Schema & entities |
| [SECURITY.md](docs/SECURITY.md) | JWT & RBAC |
| [BOOKING.md](docs/BOOKING.md) / [BOOKING_UI.md](docs/BOOKING_UI.md) | Booking engine & wizard |
| [PAYMENTS.md](docs/PAYMENTS.md) | Razorpay + verify flow |
| [EMAILS.md](docs/EMAILS.md) | Notifications / outbox |
| [REPORTS.md](docs/REPORTS.md) / [ADMIN_DASHBOARD.md](docs/ADMIN_DASHBOARD.md) | Admin analytics |
| [TESTING.md](docs/TESTING.md) / [DEPLOYMENT.md](docs/DEPLOYMENT.md) | Tests & deploy |
| [LEARNING_REVERSE_ENGINEERING.md](docs/LEARNING_REVERSE_ENGINEERING.md) | How to keep learning after the app is built |
| [PRODUCTION_CHECKLIST.md](docs/PRODUCTION_CHECKLIST.md) | Go-live checklist |

| # | Module | Status |
|---|--------|--------|
| 1–2 | Structure, DB & JPA | ✅ |
| 3 | Security & JWT | ✅ |
| 4 | Guests | ✅ |
| 5 | Rooms | ✅ |
| 6 | Booking engine | ✅ |
| 7 | Payments | ✅ |
| 8 | Emails | ✅ |
| 9 | Reports | ✅ |
| 10–13 | React auth, public site, booking UI, admin | ✅ |
| 14–15 | Testing & DevOps | ✅ |
| **16** | **Multi-hotel (StayFinder / Telangana)** | ✅ *current* |

## Readiness (learning / local demo)

| Area | Status |
|------|--------|
| Modules 1–16 code on this branch | Ready |
| Book → pay (mock Razorpay) | Ready |
| Multi-hotel search / detail | Ready |
| Overlap race (pessimistic lock) | Ready |
| Guest/booking/payment ownership | Ready (customers scoped to own email) |
| Prod fail-fast (JWT + live Razorpay) | Ready when `SPRING_PROFILES_ACTIVE=prod` |
| Optional bootstrap ADMIN | Ready via `APP_BOOTSTRAP_ADMIN_*` |
| Conflict-free compile | Ready |
| Full cloud go-live (TLS, backups, on-call) | Follow [PRODUCTION_CHECKLIST.md](docs/PRODUCTION_CHECKLIST.md) |

**Keep learning after “done”:** [docs/LEARNING_REVERSE_ENGINEERING.md](docs/LEARNING_REVERSE_ENGINEERING.md)

**Local tip:** keep `APP_RAZORPAY_MOCK_ENABLED=true`. Guest email is locked to the logged-in account.

## License

Apache 2.0
