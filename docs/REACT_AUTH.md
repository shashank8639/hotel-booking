# Module 10 Guide — React Authentication

Module 3 built the **JWT engine** on Spring Boot.  
Module 10 builds the **SPA login experience** that talks to those APIs.

> Note: Module 3 left `AuthController` unfinished. This module wires thin Auth HTTP endpoints (`/auth/login`, `/register`, `/refresh`, `/logout`, `/me`) using the existing `JwtService` / users / refresh-token tables — without changing `pom.xml`, `application.yml`, or regenerating Guest→Reports modules.

---

## 1. What JWT authentication is

JWT (JSON Web Token) is a signed string the server gives you after login. The SPA stores it and sends:

```http
Authorization: Bearer <accessToken>
```

on each API call. The server verifies signature + expiry — **no server session** (stateless).

---

## 2. React authentication flow

```
Login form → authService.login → Axios → POST /api/auth/login
  → AuthResponse { accessToken, refreshToken, user }
  → tokenStorage + AuthContext
  → navigate to role home (/admin|customer/dashboard)
```

On later requests, Axios request interceptor attaches the access token. On **401**, response interceptor tries **refresh** once, then logs out.

---

## 3. Why Context API

Login state is needed by Navbar, route guards, and pages far apart in the tree. Context avoids prop drilling. Alternatives: Redux/Zustand — Context is enough for auth in many enterprise SPAs.

---

## 4. Why Axios interceptors matter

- **Request interceptor:** attach JWT once, everywhere.  
- **Response interceptor:** central 401 / refresh / logout — pages stay clean.  
- Single-flight refresh prevents stampede when many calls fail together.

---

## 5. Protected routes vs public routes

| Route | Guard |
|-------|--------|
| `/login`, `/register`, `/rooms` | Public |
| `/customer/**` | `PrivateRoute` + `RoleRoute(CUSTOMER)` |
| `/admin/**` | `PrivateRoute` + `RoleRoute(ADMIN)` |
| `/reception/**` | `RoleRoute(RECEPTIONIST|ADMIN)` — UI ready; backend role may come later |

---

## 6. Authentication vs authorization

- **Authentication:** who are you? (login → JWT)  
- **Authorization:** what may you open? (roles → `RoleRoute` + backend `hasRole`)

Never trust UI alone — backend still enforces roles.

---

## 7. RBAC

Roles: `ADMIN`, `CUSTOMER` (backend). Frontend also understands `RECEPTIONIST` for future menus.

---

## 8. Token expiration

- Access token: short-lived; used on every call.  
- Refresh token: longer; stored DB-side; rotated on `/auth/refresh`.  
- Expired access → interceptor refresh; expired refresh → clear storage → login.

---

## 9. Storage trade-offs

| Store | Pros | Cons |
|-------|------|------|
| localStorage | Survives refresh (“Remember me”) | XSS can read it |
| sessionStorage | Cleared with tab | Still XSS-readable |
| Memory only | Harder to persist-steal | Lost on refresh |
| httpOnly cookie | Not readable by JS | Needs CSRF strategy |

This module: Remember me → `localStorage`; else `sessionStorage`.

---

## 10. XSS vs CSRF (short)

- **XSS:** attacker runs JS in your origin → can steal tokens in web storage → sanitize UI, CSP.  
- **CSRF:** browser auto-sends cookies to your API → relevant for cookie auth; JWT in `Authorization` header is not auto-sent cross-site the same way.

---

## Key frontend files

| Path | Role |
|------|------|
| `services/api.js` | Axios instance + interceptors |
| `services/authService.js` | `/auth/*` calls |
| `context/AuthContext.jsx` | Session state |
| `hooks/useAuth.js` | Consumer hook |
| `auth/tokenStorage.js` | JWT persistence |
| `routes/PrivateRoute.jsx` / `RoleRoute.jsx` | Guards |
| `pages/LoginPage.jsx` / `RegisterPage.jsx` | Forms |

---

## Run locally

```bash
# terminal 1 — API (needs MySQL + V4 auth tables seeded)
cd backend && mvn spring-boot:run

# terminal 2 — UI
cd frontend && npm install && npm run dev
```

Open `http://localhost:5173/register` then login.

```bash
cd frontend && npm test
cd backend && mvn -q test -Dtest=AuthServiceTest,AuthControllerTest
```

---

## Try this

1. Login with Remember me on/off; restart browser; observe storage.  
2. Expire access token (wait or shorten JWT) and watch refresh.  
3. Open `/admin/dashboard` as CUSTOMER → `/unauthorized`.

---

## What’s next

**Module 11 — Public Website** ([PUBLIC_WEBSITE.md](PUBLIC_WEBSITE.md)) — landing, room search, details, and book → payment handoff.

---

*Last updated: Module 10 — React Authentication*
