# Learning Modules Index

This project is built **incrementally across 8 modules**. Each module adds one layer of the application. Use this page as your navigation hub.

**Current progress:** Module 2 complete — persistence layer in place.

---

## Quick Navigation

| Module | Topic | Status | Documentation |
|--------|-------|-------|---------------|
| [1](#module-1--project-structure) | Project structure |  Done | [ARCHITECTURE.md](ARCHITECTURE.md) |
| [2](#module-2--database-design--jpa-entities) | Database & JPA entities |  Done | [DATABASE.md](DATABASE.md) |
| [3](#module-3--service-layer) | Service layer |  Next | *(coming)* |
| [4](#module-4--rest-apis--dtos) | REST APIs & DTOs |  Planned | *(coming)* |
| [5](#module-5--jwt-authentication) | JWT authentication |  Planned | *(coming)* |
| [6](#module-6--booking-business-logic) | Booking business logic |  Planned | *(coming)* |
| [7](#module-7--react-ui) | React UI |  Planned | *(coming)* |
| [8](#module-8--production-deployment) | Production deployment |  Planned | *(coming)* |

---

## Module 1 — Project Structure

**Status:**  Complete

**Goal:** Bootstrap a production-ready full-stack project skeleton with no business logic.

**Deliverables:**

| Item | Location |
|------|----------|
| Maven `pom.xml` with dependencies | `backend/pom.xml` |
| Spring Boot configuration | `backend/src/main/resources/application.yml` |
| Config skeletons (Security, CORS, OpenAPI, JWT props) | `backend/src/main/java/com/hotelbooking/config/` |
| React + Vite frontend shell | `frontend/` |
| Docker Compose skeleton | `docker-compose.yml`, `docker/` |
| Architecture overview | [ARCHITECTURE.md](ARCHITECTURE.md) |

**What you learned:** Maven, Spring Boot auto-configuration, layered package structure, Docker basics, environment-based config.

**Interview focus:** Spring Boot startup, `@SpringBootApplication`, Maven lifecycle, why separate frontend/backend.

---

## Module 2 — Database Design & JPA Entities

**Status:**  Complete

**Goal:** Design and implement the persistence layer — schema, entities, repositories.

**Deliverables:**

| Item | Location |
|------|----------|
| Flyway schema migration | `backend/src/main/resources/db/migration/V1__create_tables.sql` |
| Sample seed data | `backend/src/main/resources/db/migration/V2__seed_sample_data.sql` |
| JPA entities (Guest, Room, Booking, BookingRoom, Payment) | `backend/src/main/java/com/hotelbooking/entity/` |
| Domain enums | `backend/src/main/java/com/hotelbooking/database/` |
| Repository interfaces | `backend/src/main/java/com/hotelbooking/repository/` |
| Database documentation | [DATABASE.md](DATABASE.md) |

**What you learned:** ER design, JPA relationships, LAZY loading, BigDecimal for money, Flyway migrations, Spring Data query methods.

**Interview focus:** Entity lifecycle, `@ManyToOne` vs `@OneToMany`, cascade types, N+1 problem, `ddl-auto` strategies.

---

## Module 3 — Service Layer

**Status:** 🔜 Next

**Goal:** Add business logic and transaction boundaries on top of repositories.

**Planned deliverables:**

| Item | Location |
|------|----------|
| Service interfaces & implementations | `backend/src/main/java/com/hotelbooking/service/` |
| `@Transactional` boundaries | Service classes |
| Basic CRUD operations for all entities | GuestService, RoomService, BookingService, … |
| Custom exceptions | `backend/src/main/java/com/hotelbooking/exception/` |
| Service layer documentation | `docs/SERVICES.md` *(planned)* |

**What you'll learn:** Service layer pattern, transaction management, aggregate roots, domain validation at service level.

**Interview focus:** `@Transactional` propagation, checked vs unchecked exceptions, service vs repository responsibility.

---

## Module 4 — REST APIs & DTOs

**Status:**  Planned

**Goal:** Expose the API via REST controllers with DTOs and MapStruct mapping.

**Planned deliverables:**

| Item | Location |
|------|----------|
| REST controllers | `backend/src/main/java/com/hotelbooking/controller/` |
| Request/response DTOs | `backend/src/main/java/com/hotelbooking/dto/` |
| MapStruct mappers | `backend/src/main/java/com/hotelbooking/mapper/` |
| Global exception handler | `backend/src/main/java/com/hotelbooking/exception/` |
| API documentation | `docs/API.md` *(planned)* |

**What you'll learn:** REST conventions, DTO pattern, Bean Validation, MapStruct, OpenAPI annotations, HTTP status codes.

**Interview focus:** Why DTOs over entities, `@Valid`, idempotency, RESTful URL design.

---

## Module 5 — JWT Authentication

**Status:**  Planned

**Goal:** Secure the API with register/login and role-based access control.

**Planned deliverables:**

| Item | Location |
|------|----------|
| JWT token provider & filter | `backend/src/main/java/com/hotelbooking/security/` |
| Auth endpoints (register, login) | `AuthController` |
| Role-based authorization (USER, ADMIN) | `SecurityConfig` updates |
| Security documentation | `docs/SECURITY.md` *(planned)* |

**What you'll learn:** Stateless authentication, JWT structure, Spring Security filter chain, BCrypt, role-based access.

**Interview focus:** JWT vs sessions, filter chain order, `@PreAuthorize`, password hashing.

---

## Module 6 — Booking Business Logic

**Status:**  Planned

**Goal:** Implement core hotel booking rules — availability, conflicts, pricing, status transitions.

**Planned deliverables:**

| Item | Location |
|------|----------|
| Availability checking | `BookingService` |
| Date overlap validation | Booking rules |
| Price calculation | Booking + BookingRoom logic |
| Booking status workflow | PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT |
| Business logic documentation | `docs/BOOKING.md` *(planned)* |

**What you'll learn:** Domain-driven design basics, complex queries, transaction isolation, optimistic locking.

**Interview focus:** Handling concurrent bookings, date range overlap SQL, aggregate design.

---

## Module 7 — React UI

**Status:**  Planned

**Goal:** Build the frontend — search, book, manage reservations, auth screens.

**Planned deliverables:**

| Item | Location |
|------|----------|
| Pages (Home, Search, Booking, Login, Dashboard) | `frontend/src/pages/` |
| Reusable components | `frontend/src/components/` |
| Axios API clients | `frontend/src/services/` |
| Auth context & protected routes | `frontend/src/context/` |
| Frontend documentation | `docs/FRONTEND.md` *(planned)* |

**What you'll learn:** React Router, Material UI, Axios interceptors, JWT storage, form handling, API integration.

**Interview focus:** SPA architecture, CORS, token refresh, component composition.

---

## Module 8 — Production Deployment

**Status:**  Planned

**Goal:** Harden Docker setup, add health checks, environment profiles, and deployment runbook.

**Planned deliverables:**

| Item | Location |
|------|----------|
| Production Dockerfiles | `docker/` |
| Flyway integration in `pom.xml` | Automated migrations |
| Health checks & Actuator | `/actuator/health` |
| Production `application-prod.yml` | Environment profile |
| Deployment documentation | `docs/DEPLOYMENT.md` *(planned)* |

**What you'll learn:** Multi-stage Docker builds, secrets management, CI/CD basics, production config profiles.

**Interview focus:** Container orchestration, 12-factor app, blue-green deployment, database migration in CI.

---

## How Modules Connect

```
Module 1          Module 2           Module 3           Module 4
Project Setup  →  Entities/Repos  →  Services       →  Controllers/DTOs
(skeleton)        (persistence)      (business logic)    (REST API)
                                                          │
Module 8          Module 7           Module 6           Module 5
Deployment     ←  React UI       ←  Booking Logic  ←  JWT Security
(Docker/CI)       (frontend)         (domain rules)      (auth)
```

Each module builds on the previous. Do not skip ahead — later modules assume earlier layers exist.

---

## Documentation Map

| Document | Scope |
|----------|-------|
| [README.md](../README.md) | Quick start, env vars, high-level overview |
| [MODULES.md](MODULES.md) | This file — module index and progress |
| [ARCHITECTURE.md](ARCHITECTURE.md) | System architecture, package structure, design principles |
| [DATABASE.md](DATABASE.md) | Module 2 — schema, ER diagram, migrations |
| `docs/SERVICES.md` | Module 3 — *(planned)* |
| `docs/API.md` | Module 4 — *(planned)* |
| `docs/SECURITY.md` | Module 5 — *(planned)* |
| `docs/BOOKING.md` | Module 6 — *(planned)* |
| `docs/FRONTEND.md` | Module 7 — *(planned)* |
| `docs/DEPLOYMENT.md` | Module 8 — *(planned)* |

---

## Suggested Learning Path

1. Read the **concepts section** in each module's chat/lesson before reviewing code.
2. Explore the **deliverables** in the codebase for that module.
3. Complete the **practice assignments** before moving on.
4. Answer the **interview questions** — have them evaluated before the next module.
5. Update this page's status as you progress *(or ask the agent to update it)*.

---

*Last updated: Module 2 complete*
