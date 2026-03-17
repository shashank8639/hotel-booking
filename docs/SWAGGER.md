# Swagger / OpenAPI Usage Guide

SpringDoc serves OpenAPI 3 for this API.

## URLs

| Profile | Swagger UI | OpenAPI JSON |
|---------|------------|--------------|
| default / `dev` (`context-path=/api`) | http://localhost:8080/api/swagger-ui.html | http://localhost:8080/api/v3/api-docs |
| `prod` (`context-path=/api/v1`) | **Disabled** (`springdoc.*.enabled=false`) | **Disabled** |
| Via Nginx (dev) | http://localhost/api/swagger-ui.html | http://localhost/api/v3/api-docs |
| Via Nginx (prod) | Nginx returns **404** for swagger/api-docs paths | same |

## Production restriction (Module 15)

Two layers:

1. **`application-prod.yml`** — `springdoc.api-docs.enabled=false` and `springdoc.swagger-ui.enabled=false`  
2. **`docker/nginx.prod.conf`** — `location` regex returns 404 for `/api/v1/swagger-ui` and `/api/v1/v3/api-docs`

Use Swagger only on local/dev (or a private staging stack with `dev` profile).

---

## Authenticate in Swagger

1. `POST /auth/login` (or register) → copy `accessToken`  
2. Click **Authorize**  
3. Enter: `Bearer <accessToken>` (or only the token if the UI adds Bearer)  
4. Try `GET /guests` or admin routes with an **ADMIN** user  

---

## API categories (tags)

Typical tags in this project:

- **Auth** — register, login, refresh, password reset  
- **Guests** — CRUD  
- **Rooms** — search, details, admin room ops  
- **Bookings** — create, cancel, availability  
- **Payments** — create-order, verify, refund, webhook, invoice  
- **Admin Reports** — dashboard, revenue, occupancy, bookings, monthly, payments  

---

## How to test APIs

1. Happy path: register → login → search rooms → create booking → create-order → verify (mock Razorpay)  
2. Negative: call admin report as CUSTOMER → expect 403  
3. Use Postman collection in `postman/` for scripted flows  

---

## Production note

**Prod profile disables Swagger/OpenAPI** (see above). Staging should use a non-prod profile if product managers need interactive docs.

