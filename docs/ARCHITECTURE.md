<<<<<<< HEAD
# Hotel Booking System — Architecture

## Overview

This is a **full-stack, production-oriented** hotel booking platform. The codebase follows **layered / clean architecture** principles so each concern stays isolated and testable.
=======
# Architecture Guide — How the System Fits Together

Read this when you feel lost in folders. It is the **map of the city**, not every street.

**How to use it:**
1. Look at the layer diagram — say out loud what each layer is for  
2. Match packages in the table to folders on disk  
3. When you add a feature, ask: *“Which layer does this change belong in?”*

Detailed teaching for each module lives in [MODULES.md](MODULES.md).

---

## Overview

This is a **full-stack, production-oriented** hotel booking platform. The codebase follows **layered / clean architecture** so each concern stays isolated and testable — the same style used in most Spring Boot teams.
>>>>>>> feature/module-1-foundation-practice

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  React (Vite) + Material UI + React Router + Axios          │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/JSON (REST)
┌──────────────────────────▼──────────────────────────────────┐
│                      API Layer (Spring Boot)                   │
│  Controllers → DTOs + Validation + Swagger                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    Business Layer                              │
│  Services (booking rules, availability, pricing logic)        │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                   Persistence Layer                            │
│  Repositories (Spring Data JPA) → Entities → MySQL           │
└─────────────────────────────────────────────────────────────┘
```

## Backend Package Structure

| Package | Responsibility | Module |
|---------|----------------|--------|
<<<<<<< HEAD
| `config` | Cross-cutting Spring configuration (Security, CORS, OpenAPI, JWT props) | 1 |
| `database` | Domain enums (`BookingStatus`, `RoomType`, …) | 2 |
| `entity` | JPA/Hibernate domain models mapped to DB tables | 2 |
| `repository` | Spring Data JPA repository interfaces | 2 |
| `migration` | Flyway documentation; SQL scripts in `resources/db/migration/` | 2 |
| `controller` | REST endpoints — HTTP in/out only | 4 |
| `service` | Business logic and transaction boundaries | 3 |
| `dto` | Data Transfer Objects — API contract, decoupled from entities | 4 |
| `mapper` | MapStruct interfaces for Entity ↔ DTO conversion | 4 |
| `security` | JWT filters, UserDetails, authentication helpers | 5 |
=======
| `config` | Cross-cutting Spring configuration (Security, CORS, OpenAPI, JWT props) | 1 / 3 |
| `database` | Domain enums (`BookingStatus`, `RoomType`, …) | 2 |
| `entity` | JPA domain models (Guest, Room, Booking, User, …) | 2 / 3 |
| `repository` | Spring Data JPA repository interfaces | 2 / 3 / 4 |
| `migration` | Flyway documentation; SQL scripts in `resources/db/migration/` | 2 / 3 |
| `security` | JWT filters, UserDetails, token utilities | 3 |
| `controller` | REST endpoints — HTTP in/out only | 4 |
| `service` | Business logic and transaction boundaries | 4 |
| `dto` | Data Transfer Objects — API contract, decoupled from entities | 4 |
| `mapper` | MapStruct interfaces for Entity ↔ DTO conversion | 4 |
>>>>>>> feature/module-1-foundation-practice
| `exception` | Global exception handling, custom error types | 4 |
| `util` | Shared helpers (dates, constants) | — |

## Frontend Folder Structure

| Folder | Responsibility |
|--------|----------------|
| `components` | Reusable UI building blocks (Navbar, RoomCard, DatePicker) |
| `pages` | Route-level screens (Home, Search, Booking, Login) |
| `services` | Axios API clients (authService, hotelService) |
| `hooks` | Custom React hooks (useAuth, useBooking) |
| `context` | Global state (AuthContext) |
| `utils` | Formatters, validators, constants |
| `assets` | Images, icons, static files |

## Design Principles

1. **Separation of concerns** — UI never talks to the database directly.
2. **DTO pattern** — Never expose JPA entities over REST (avoids lazy-loading leaks).
3. **Stateless API** — JWT tokens, no server-side sessions.
4. **Configuration externalization** — Secrets and URLs via environment variables.
5. **Profile-based config** — `dev`, `test`, `prod` profiles for different environments.

See [MODULES.md](MODULES.md) for the incremental learning path across all 8 modules.

## Persistence Layer (Module 2 — Complete)

Five entities model the core booking domain:

```
Guest (1) ──< Booking (1) ──< BookingRoom (N) >── Room
                  │
                  └──< Payment
```

- **SQL migrations:** `backend/src/main/resources/db/migration/V1__create_tables.sql`
- **Full database docs:** [DATABASE.md](DATABASE.md)

Key design choices: `BigDecimal` for money, `LocalDate` for stay dates, lazy fetching on all relationships, price snapshot on `booking_rooms`.

<<<<<<< HEAD
## Module Roadmap

| Module | Focus | Status |
|--------|-------|-------|
| 1 | Project structure |  Done |
| 2 | Database design & JPA entities |  Done |
| 3 | Service layer & transactions | Next |
| 4 | REST controllers & DTOs | Planned |
| 5 | JWT authentication & authorization | Planned |
| 6 | Booking business logic | Planned |
| 7 | React UI pages & API integration | Planned |
| 8 | Docker production setup & deployment | Planned |
=======
## Security Layer (Module 3 — Complete)

Stateless JWT authentication with RBAC (`ADMIN`, `CUSTOMER`), refresh tokens, and BCrypt password hashing.

- **Docs:** [SECURITY.md](SECURITY.md)
- **Migration:** `V4__create_auth_tables.sql`

## Guest Management (Module 4 — Complete)

First production REST domain: Guest CRUD, validation, pagination, search, unit tests.

- **Docs:** [GUESTS.md](GUESTS.md)
- **Endpoints:** `/api/guests`

## Module Roadmap

| Module | Focus | Status |
|--------|-------|--------|
| 1 | Project structure | ✅ Done |
| 2 | Database design & JPA entities | ✅ Done |
| 3 | Security & JWT authentication | ✅ Done |
| 4 | Guest Management (CRUD APIs) | ✅ Done |
| 5 | Room Management | 🔜 Next |
| 6 | Booking business logic | Planned |
| 7 | React UI pages & API integration | Planned |
| 8 | Docker production setup & deployment | Planned |

See [MODULES.md](MODULES.md) for the full learning path.
>>>>>>> feature/module-1-foundation-practice
