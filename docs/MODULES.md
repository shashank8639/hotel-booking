<<<<<<< HEAD
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
=======
# Your Learning Path — Hotel Booking System

Welcome. This is not just a project dump — it is a **guided path** to become capable of building enterprise Java full-stack apps on your own.

Read this file first. Then open the module docs in order. Each doc teaches *why*, then shows *where in the code*, then tells you *what to try next*.

---

## How to use these docs

Think of each module like a chapter with a mentor sitting beside you:

1. **Read the “Why this exists” section** before opening the code  
2. **Open the files listed** and compare them to the explanation  
3. **Do the “Try this” exercises** yourself  
4. **Answer the interview questions** out loud (or write them down)  
5. Only then move to the next module  

Do **not** skip ahead. Later modules assume you understand earlier ones.

---

## Where you are now

| Module | What you built | Guide |
|--------|----------------|-------|
| 1 | Project skeleton | [ARCHITECTURE.md](ARCHITECTURE.md) |
| 2 | Database & entities | [DATABASE.md](DATABASE.md) |
| 3 | Security & JWT | [SECURITY.md](SECURITY.md) |
| 4 | Guest Management APIs | [GUESTS.md](GUESTS.md) |
| 5 | Room Management | [ROOMS.md](ROOMS.md) ← **you are here** |
| 6–8 | Booking, React, Deploy | Later |

---

## The big picture (keep this in your head)

```
You are building a hotel system the way companies do it:

  Module 1  →  Empty house (folders, tools, Docker)
  Module 2  →  Foundation (tables & Java entities)
  Module 3  →  Front door lock (who can enter — JWT/security)
  Module 4  →  First real room (Guest CRUD APIs)
  Module 5  →  Inventory (Rooms)
  Module 6  →  Business of booking
  Module 7  →  Website (React)
  Module 8  →  Ship to production
```

Every request will eventually travel like this:

```
Browser → React → Axios → Controller → Service → Repository → MySQL
                              ↑
                     Security filter checks JWT first
```
>>>>>>> feature/module-1-foundation-practice

---

## Module 1 — Project Structure

<<<<<<< HEAD
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
=======
**Status:** Done  

**Mentor note:** You set up the *workshop* before building the furniture. That is how senior engineers start — structure first, features later.

**What to open:**
- `backend/pom.xml` — what libraries the project depends on  
- `backend/src/main/resources/application.yml` — how the app is configured  
- `docker-compose.yml` — how MySQL + API + UI can run together  

**What you should be able to explain:**
- Why backend and frontend are separate folders  
- What Maven does  
- Why we use `application.yml` instead of hardcoding values  

**Guide:** [ARCHITECTURE.md](ARCHITECTURE.md)

---

## Module 2 — Database & JPA Entities

**Status:** Done  

**Mentor note:** Business logic without a solid data model is fragile. We designed tables and relationships *before* writing services.

**What to open:**
- `entity/Guest.java`, `Booking.java`, `Room.java`  
- `db/migration/V1__create_tables.sql`  
- `repository/` interfaces  

**What you should be able to explain:**
- Entity vs table  
- `@ManyToOne` owning side vs `mappedBy`  
- Why `BigDecimal` for money and `LocalDate` for dates  
- Why Flyway (or SQL scripts) beat `ddl-auto=update` in production  

**Guide:** [DATABASE.md](DATABASE.md)

**Try this:**
1. Draw the ER diagram from memory  
2. Explain why `BookingRoom` exists (hint: price snapshot)  

---

## Module 3 — Security & JWT Authentication

**Status:** Done  

**Mentor note:** We locked the API *before* adding many endpoints. In real companies, security is not an afterthought.

**What you built (infrastructure, not login UI yet):**
- JWT create/validate (`JwtService`)  
- Filter that reads `Authorization: Bearer ...`  
- Roles: ADMIN / CUSTOMER  
- Tables: `users`, `roles`, `refresh_tokens`, `password_reset_tokens`  

**Important:** Login/register *HTTP endpoints* are not wired yet. The *engine* is ready; the *steering wheel* (AuthController) comes later.

**Guide:** [SECURITY.md](SECURITY.md) — written as a walkthrough.

**Try this:**
1. Trace one request through `JwtAuthenticationFilter` on paper  
2. Explain access token vs refresh token to a friend  

---

## Module 4 — Guest Management

**Status:** Done  

**Mentor note:** This is your first full vertical slice: **Controller → Service → Repository → Database**, with validation, pagination, search, and tests.

**Guide:** [GUESTS.md](GUESTS.md)

---

## Module 5 — Room Management (current)

**Status:** Done  

**Mentor note:** Same layered pattern as Guests, plus pricing, availability state machine, image metadata, and **admin vs public** API split.

**What to open:**
- `controller/RoomController.java` + `AdminRoomController.java`  
- `service/impl/RoomServiceImpl.java` — transitions + pricing rules  
- `entity/RoomImage.java` — URL-based images  
- `db/migration/V5__room_management_enhancements.sql`  

**Guide:** [ROOMS.md](ROOMS.md)

**Try this:**
1. Trace `AVAILABLE → RESERVED` in the service  
2. Call `GET /api/rooms/search` with two filters  
3. Run Room unit tests  

**Why after Guest?**  
A booking needs a **guest** and a **room**. Guest APIs first, room inventory second, then booking logic.
>>>>>>> feature/module-1-foundation-practice

---

## Module 6 — Booking Business Logic

<<<<<<< HEAD
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
=======
Here the real hotel rules appear: date overlap, availability, pricing, status transitions. Modules 2–5 make this possible.
>>>>>>> feature/module-1-foundation-practice

---

## Module 7 — React UI

<<<<<<< HEAD
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
=======
Wire screens to the APIs you already trust. Frontend is easier when the backend contract is stable.
>>>>>>> feature/module-1-foundation-practice

---

## Module 8 — Production Deployment

<<<<<<< HEAD
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
=======
Docker hardening, env profiles, health checks — ship what you built.

---

## How the modules connect

```
Module 1 → Module 2 → Module 3 → Module 4 → Module 5
 Setup      Data       Security    Guests      Rooms
                                      \        /
                                       \      /
                                     Module 6
                                      Booking
                                         |
                                     Module 7
                                       React
                                         |
                                     Module 8
                                      Deploy
```

---

## Documentation map (what to read when)

| When you want to… | Open |
|-------------------|------|
| Understand the learning order | This file (`MODULES.md`) |
| See system layers | [ARCHITECTURE.md](ARCHITECTURE.md) |
| Learn the database | [DATABASE.md](DATABASE.md) |
| Learn JWT & security | [SECURITY.md](SECURITY.md) |
| Learn Guest CRUD APIs | [GUESTS.md](GUESTS.md) |
| Learn Room Management | [ROOMS.md](ROOMS.md) |

---

## Suggested weekly rhythm

| Day | Activity |
|-----|----------|
| 1 | Read module guide + open listed files |
| 2 | Draw the flow on paper (request → DB → response) |
| 3 | Do “Try this” tasks / break something and fix it |
| 4 | Answer interview questions without looking |
| 5 | Summarize the module in 10 lines in your own words |

---

*Last updated: Module 5 complete — Room Management*
>>>>>>> feature/module-1-foundation-practice
