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
| `config` | Cross-cutting Spring configuration (Security, CORS, OpenAPI, JWT, Razorpay props, Async) | 1 / 3 / 7 |
| `database` | Domain enums (`BookingStatus`, `RoomType`, `PaymentStatus`, …) | 2 / 7 |
| `entity` | JPA domain models (Guest, Room, Booking, Payment, …) | 2 / 3 / 7 |
| `repository` | Spring Data JPA repository interfaces | 2–7 |
| `migration` | Flyway documentation; SQL scripts in `resources/db/migration/` | 2 / 3 / 5 / 7 |
| `security` | JWT filters, UserDetails, token utilities | 3 |
| `payment` | Razorpay gateway client, HMAC signature helpers (no SDK) | 7 |
| `notification` | Email templates, async notifications, mail transport abstraction | 8 |
| `controller` | REST endpoints — HTTP in/out only | 4–9 |
| `service` | Business logic and transaction boundaries | 4–9 |
| `dto` / `dto.report` | API contracts; reporting chart payloads | 4–9 |
| `mapper` | MapStruct interfaces for Entity ↔ DTO conversion | 4–7 |
| `exception` | Global exception handling, custom error types | 4–9 |
| `util` | Shared helpers (dates, invoice numbers, report ranges) | 6 / 7 / 9 |

## Frontend Folder Structure

| Folder | Responsibility |
|--------|----------------|
| `components` / `components/booking` | Reusable UI (Navbar, RoomCard, booking steps) |
| `pages` / `pages/booking` | Route-level screens (Home, Search, wizard, payment) |
| `services` | Axios API clients (auth, room, booking, guest, payment) |
| `hooks` | Custom React hooks (useAuth, useBookingWizard, usePaymentCheckout) |
| `context` | Global / draft state (AuthContext, BookingWizardContext) |
| `layouts` | Auth, main nav, booking stepper shell |
| `utils` | Formatters, validators, price math, mock payment sign |
| `assets` | Images, icons, static files |

## Design Principles

1. **Separation of concerns** — UI never talks to the database directly.
2. **DTO pattern** — Never expose JPA entities over REST (avoids lazy-loading leaks).
3. **Stateless API** — JWT tokens, no server-side sessions.
4. **Configuration externalization** — Secrets and URLs via environment variables.
5. **Profile-based config** — `dev`, `test`, `prod` profiles for different environments.

See [MODULES.md](MODULES.md) for the incremental learning path.

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

## Security Layer (Module 3 — Complete)

Stateless JWT authentication with RBAC (`ADMIN`, `CUSTOMER`), refresh tokens, and BCrypt password hashing.

- **Docs:** [SECURITY.md](SECURITY.md)
- **Migration:** `V4__create_auth_tables.sql`

## Guest Management (Module 4 — Complete)

First production REST domain: Guest CRUD, validation, pagination, search, unit tests.

- **Docs:** [GUESTS.md](GUESTS.md)
- **Endpoints:** `/api/guests`

## Room Management (Module 5 — Complete)

Inventory APIs: public catalog + admin mutations, pricing, availability, images.

- **Docs:** [ROOMS.md](ROOMS.md)
- **Endpoints:** `/api/rooms`, `/api/admin/rooms`

## Booking Engine (Module 6 — Complete)

Reservations with overlap checks, price snapshots, status workflow, locks.

- **Docs:** [BOOKING.md](BOOKING.md)
- **Endpoints:** `/api/bookings`

## Payment Management (Module 7 — Complete)

Razorpay-style orders, HMAC verify, webhooks, refunds, invoices/PDF, async receipt stub.

- **Docs:** [PAYMENTS.md](PAYMENTS.md)
- **Endpoints:** `/api/payments`
- **Migration:** `V6__payment_management_enhancements.sql`

## Email Notifications (Module 8 — Complete)

HTML email templates, invoice PDF attachments, async delivery, outbox/log transport (Spring Mail-ready).

- **Docs:** [EMAILS.md](EMAILS.md)
- **Package:** `com.hotelbooking.notification`

## Reports & Admin Dashboard (Module 9 — Complete)

Admin KPIs and analytics: revenue, occupancy, bookings, monthly rollups, payment charts.

- **Docs:** [REPORTS.md](REPORTS.md)
- **Endpoints:** `/admin/dashboard`, `/admin/reports/**`
- **Key types:** `AdminReportController`, `ReportService`, `ReportQueryRepository`

## React Authentication (Module 10 — Complete)

SPA login/register, AuthContext, Axios JWT interceptors, protected + role routes.

- **Docs:** [REACT_AUTH.md](REACT_AUTH.md)
- **Frontend:** `frontend/src/context`, `services/api.js`, `pages/LoginPage.jsx`
- **Backend HTTP (wires Module 3 engine):** `AuthController` — `/auth/login|register|refresh|logout|me`

## Public Website (Module 11 — Complete)

Landing, room search/filters/pagination, room details, book entry (single-property inventory).

- **Docs:** [PUBLIC_WEBSITE.md](PUBLIC_WEBSITE.md)
- **Routes:** `/`, `/rooms`, `/rooms/:id`, `/book` (enhanced in Module 12)

## Booking UI (Module 12 — Complete)

Multi-step wizard: stay details → guest → summary → payment → success/failure; price breakdown; invoice download.

- **Docs:** [BOOKING_UI.md](BOOKING_UI.md)
- **Routes:** `/book`, `/book/payment/:id`, `/book/success/:id`, `/book/failure/:id` (`/checkout/:id` redirects)

## React Admin Dashboard (Module 13 — Complete)

Admin portal shell, KPI home, Recharts, rooms/guests/bookings/payments management, reports tabs.

- **Docs:** [ADMIN_DASHBOARD.md](ADMIN_DASHBOARD.md)
- **Routes:** `/admin/dashboard`, `/admin/rooms`, `/admin/guests`, `/admin/bookings`, `/admin/payments`, `/admin/reports`


## Testing (Module 14 — Complete)

Automated safety net across backend and frontend:

- **Backend:** JUnit 5 + Mockito unit tests, `@DataJpaTest` repositories, `@SpringBootTest` + MockMvc security/integration flows
- **Frontend:** Vitest + React Testing Library (`src/test` + `src/tests`), coverage via `npm run test:coverage`
- **Utilities:** `TestDataFactory`, builders, `IntegrationTestSupport`

Guide: [TESTING.md](TESTING.md)

## Production Deployment (Module 15 — Complete)

Docker multi-stage images, Compose (dev/prod), Nginx reverse proxy, GitHub Actions CI + CD template, Postman, ops docs.

- **Docs:** [DEPLOYMENT.md](DEPLOYMENT.md), [INSTALLATION.md](INSTALLATION.md), [ENVIRONMENT.md](ENVIRONMENT.md)
- **Paths:** `docker/`, `docker-compose*.yml`, `.github/workflows/`, `scripts/`, `postman/`


## Module Roadmap

| Module | Focus | Status |
|--------|-------|--------|
| 1 | Project structure | ✅ Done |
| 2 | Database design & JPA entities | ✅ Done |
| 3 | Security & JWT authentication | ✅ Done |
| 4 | Guest Management (CRUD APIs) | ✅ Done |
| 5 | Room Management | ✅ Done |
| 6 | Booking Engine | ✅ Done |
| 7 | Payment Management | ✅ Done |
| 8 | Email Notifications | ✅ Done |
| 9 | Reports & Admin Dashboard APIs | ✅ Done |
| 10 | React Authentication | ✅ Done |
| 11 | Public Website | ✅ Done |
| 12 | Booking UI | ✅ Done |
| 13 | React Admin Dashboard | ✅ Done |
| 14 | Testing (JUnit, Mockito, Integration, Vitest) | ✅ Done |
| 15 | Production Deployment (Docker, CI/CD, Nginx) | ✅ Done *(current)* |

See [MODULES.md](MODULES.md) for the full learning path.
