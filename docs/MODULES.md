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

---

## Module 1 — Project Structure

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

---

## Module 6 — Booking Business Logic

Here the real hotel rules appear: date overlap, availability, pricing, status transitions. Modules 2–5 make this possible.

---

## Module 7 — React UI

Wire screens to the APIs you already trust. Frontend is easier when the backend contract is stable.

---

## Module 8 — Production Deployment

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
