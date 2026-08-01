# Learning Modules Index

This project is built **incrementally across 8 modules**. Each module adds one layer of the application. Use this page as your navigation hub.

**Current progress:** Module 4 complete — Guest Management APIs in place.

---

## Quick Navigation

| Module | Topic | Status | Documentation |
|--------|-------|--------|---------------|
| [1](#module-1--project-structure) | Project structure | ✅ Done | [ARCHITECTURE.md](ARCHITECTURE.md) |
| [2](#module-2--database-design--jpa-entities) | Database & JPA entities | ✅ Done | [DATABASE.md](DATABASE.md) |
| [3](#module-3--security--jwt-authentication) | Security & JWT authentication | ✅ Done | [SECURITY.md](SECURITY.md) |
| [4](#module-4--guest-management) | Guest Management (CRUD APIs) | ✅ Done *(current)* | [GUESTS.md](GUESTS.md) |
| [5](#module-5--room-management) | Room Management | 🔜 Next | *(coming)* |
| [6](#module-6--booking-business-logic) | Booking business logic | ⬜ Planned | *(coming)* |
| [7](#module-7--react-ui) | React UI | ⬜ Planned | *(coming)* |
| [8](#module-8--production-deployment) | Production deployment | ⬜ Planned | *(coming)* |

---

## Module 1 — Project Structure

**Status:** ✅ Complete

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

**Status:** ✅ Complete

**Goal:** Design and implement the persistence layer — schema, entities, repositories.

**Deliverables:**

| Item | Location |
|------|----------|
| Flyway schema migration | `backend/src/main/resources/db/migration/V1__create_tables.sql` |
| Sample seed data | `backend/src/main/resources/db/migration/V2__seed_sample_data.sql` |
| Optimistic locking + index | `V3__add_booking_version_and_index.sql` |
| JPA entities (Guest, Room, Booking, BookingRoom, Payment) | `backend/src/main/java/com/hotelbooking/entity/` |
| Domain enums | `backend/src/main/java/com/hotelbooking/database/` |
| Repository interfaces | `backend/src/main/java/com/hotelbooking/repository/` |
| Database documentation | [DATABASE.md](DATABASE.md) |

**What you learned:** ER design, JPA relationships, LAZY loading, BigDecimal for money, Flyway migrations, Spring Data query methods.

**Interview focus:** Entity lifecycle, `@ManyToOne` vs `@OneToMany`, cascade types, N+1 problem, `ddl-auto` strategies.

---

## Module 3 — Security & JWT Authentication

**Status:** ✅ Complete

**Goal:** Implement the authentication and authorization infrastructure (stateless JWT, RBAC, refresh tokens).

**Deliverables:**

| Item | Location |
|------|----------|
| User, Role, RefreshToken, PasswordResetToken entities | `backend/src/main/java/com/hotelbooking/entity/` |
| Auth repositories | `UserRepository`, `RoleRepository`, `RefreshTokenRepository`, … |
| JWT service, filter, entry point | `backend/src/main/java/com/hotelbooking/security/` |
| SecurityConfig (RBAC, stateless) | `backend/src/main/java/com/hotelbooking/config/SecurityConfig.java` |
| Flyway V4 auth tables | `backend/src/main/resources/db/migration/V4__create_auth_tables.sql` |
| Security documentation | [SECURITY.md](SECURITY.md) |

**Not in this module (planned later):** Auth REST endpoints (`/auth/login`, `/auth/register`) and AuthService — wired when API layer expands.

**What you learned:** Authentication vs authorization, JWT structure, filter chain, BCrypt, RBAC, refresh tokens.

**Interview focus:** Session vs JWT, `UserDetailsService`, `SecurityFilterChain`, role vs authority, OWASP auth risks.

---

## Module 4 — Guest Management

**Status:** ✅ Complete *(current)*

**Goal:** Production-ready Guest CRUD APIs with validation, pagination, sorting, search, and unit tests.

**Deliverables:**

| Item | Location |
|------|----------|
| GuestRepository (search methods) | `backend/src/main/java/com/hotelbooking/repository/GuestRepository.java` |
| GuestService + GuestServiceImpl | `backend/src/main/java/com/hotelbooking/service/` |
| GuestController | `backend/src/main/java/com/hotelbooking/controller/GuestController.java` |
| GuestRequest / GuestResponse DTOs | `backend/src/main/java/com/hotelbooking/dto/` |
| GuestMapper (MapStruct) | `backend/src/main/java/com/hotelbooking/mapper/GuestMapper.java` |
| Global exception handler | `backend/src/main/java/com/hotelbooking/exception/` |
| Unit tests (repo, service, controller) | `backend/src/test/java/com/hotelbooking/` |
| Guest API documentation | [GUESTS.md](GUESTS.md) |

**What you learned:** Layered CRUD, Bean Validation, pagination/sorting, search APIs, MapStruct, MockMvc/Mockito testing.

**Interview focus:** DTO vs entity, `Pageable`, `@Valid`, HTTP status codes, thin controllers, service business rules.

---

## Module 5 — Room Management

**Status:** 🔜 Next

**Goal:** CRUD + search for rooms (room number, type, price, status) — inventory for bookings.

**Planned deliverables:**

| Item | Location |
|------|----------|
| RoomService, RoomController, Room DTOs | `service/`, `controller/`, `dto/` |
| Room search by type/status | Repository + APIs |
| Room documentation | `docs/ROOMS.md` *(planned)* |

**What you'll learn:** Inventory APIs, enum filters, uniqueness on room number, linking rooms to bookings later.

**Interview focus:** Soft delete vs hard delete, availability vs status fields.

---

## Module 6 — Booking Business Logic

**Status:** ⬜ Planned

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

**Status:** ⬜ Planned

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

**Status:** ⬜ Planned

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
Module 1          Module 2           Module 3              Module 4
Project Setup  →  Entities/Repos  →  Security / JWT   →  Guest CRUD APIs
(skeleton)        (persistence)      (auth infrastructure)   (first domain API)
                                                              │
Module 8          Module 7           Module 6              Module 5
Deployment     ←  React UI       ←  Booking Logic     ←  Room Management
(Docker/CI)       (frontend)         (domain rules)        (inventory APIs)
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
| [SECURITY.md](SECURITY.md) | Module 3 — JWT, RBAC, auth tables |
| [GUESTS.md](GUESTS.md) | Module 4 — Guest Management APIs |
| `docs/ROOMS.md` | Module 5 — *(planned)* |
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

*Last updated: Module 4 complete (Guest Management)*
