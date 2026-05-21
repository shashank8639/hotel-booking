# Hotel Booking System — Architecture

## Overview

This is a **full-stack, production-oriented** hotel booking platform. The codebase follows **layered / clean architecture** principles so each concern stays isolated and testable.

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

| Package | Responsibility |
|---------|----------------|
| `config` | Cross-cutting Spring configuration (Security, CORS, OpenAPI, JWT props) |
| `controller` | REST endpoints — HTTP in/out only |
| `service` | Business logic and transaction boundaries |
| `repository` | Database access via JPA |
| `entity` | JPA/Hibernate domain models mapped to DB tables |
| `dto` | Data Transfer Objects — API contract, decoupled from entities |
| `mapper` | MapStruct interfaces for Entity ↔ DTO conversion |
| `security` | JWT filters, UserDetails, authentication helpers |
| `exception` | Global exception handling, custom error types |
| `util` | Shared helpers (dates, constants) |

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

## Module Roadmap

| Module | Focus |
|--------|-------|
| 1 | Project structure (current) |
| 2 | Database design & JPA entities |
| 3 | Repositories & basic CRUD services |
| 4 | REST controllers & DTOs |
| 5 | JWT authentication & authorization |
| 6 | Booking business logic |
| 7 | React UI pages & API integration |
| 8 | Docker production setup & deployment |
