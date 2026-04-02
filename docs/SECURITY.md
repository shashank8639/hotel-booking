# Module 3 Guide — Security & JWT (Learn by Walking Through)

This guide explains authentication the way you would learn it on a team: first the *idea*, then the *moving parts*, then *what is done vs not done yet*.

---

## 1. What problem does this module solve?

Your API will eventually hold guest data, bookings, and payments.

Without security:
- Anyone could call `DELETE /guests/1`  
- Anyone could see payments  
- You could never tell *who* made a change  

So Module 3 builds the **front door lock**:
- Prove who you are (**authentication**)  
- Decide what you may do (**authorization**)  

---

## 2. Two words you must never confuse

| Word | Question | Example |
|------|----------|---------|
| **Authentication** | Who are you? | Login with email/password → get JWT |
| **Authorization** | What can you do? | ADMIN can access `/payments/**`; CUSTOMER cannot |

Order is always: authenticate first, then authorize.

---

## 3. How a secured request travels (draw this)

```
Browser / Postman
   |  Authorization: Bearer eyJhbG...
   v
JwtAuthenticationFilter
   |  Is token valid? signature + expiry + type=ACCESS?
   v
SecurityContext  (stores logged-in user + roles)
   |
   v
SecurityConfig rules
   |  Is this URL public? Does role match?
   v
Controller → Service → Repository → MySQL
```

If the token is missing/invalid → **401 Unauthorized**  
If the token is valid but role is wrong → **403 Forbidden**

---

## 4. Guided tour — open files in this order

### Step A — The policy center

Open: `config/SecurityConfig.java`

This file answers:
- Which URLs are public (`/auth/**`, Swagger, `GET /rooms/**`)?  
- Which need ADMIN?  
- Are we stateful (sessions) or **stateless (JWT)**?  

We use:

```java
SessionCreationPolicy.STATELESS
```

Meaning: no server session. Each request must bring its own JWT.

---

### Step B — The doorman (filter)

Open: `security/JwtAuthenticationFilter.java`

On every request it:
1. Looks for header `Authorization: Bearer <token>`  
2. Checks it is an **access** token  
3. Loads the user  
4. Puts authentication into `SecurityContextHolder`  
5. Passes the request down the chain  

**Mentor tip:** Filters run *before* controllers. That is why security can block bad requests early.

---

### Step C — Token factory

Open: `security/JwtService.java` (implements `TokenProvider`)

Responsibilities:
- Create access JWT (short-lived, used on every API call)  
- Create refresh JWT (longer-lived, used only to get a new access token)  
- Validate signature + expiry  

JWT shape (conceptually):

```
header.payload.signature
```

Payload includes subject (email), roles, type (`ACCESS` / `REFRESH`), expiry.

---

### Step D — Bridge to your database users

Open:
- `security/CustomUserDetails.java`  
- `security/CustomUserDetailsService.java`  
- `entity/User.java`  
- `repository/UserRepository.java`

Spring Security does not know your `User` table.  
`UserDetailsService` is the adapter: **email → User row → authorities (`ROLE_ADMIN`)**.

**Important:** `User` (login account) is not the same as `Guest` (hotel customer profile). They solve different problems.

---

### Step E — Roles

Open: `security/UserRole.java` and table seed in `V4__create_auth_tables.sql`

| Role | Typical access in our config |
|------|------------------------------|
| `ADMIN` | `/admin/**`, `/payments/**` |
| `CUSTOMER` | `/bookings/**`, other authenticated routes |

Public today: auth paths (for future login), Swagger, `GET /rooms/**`.

---

### Step F — Refresh & reset tables (ready for later services)

Open entities:
- `RefreshToken`  
- `PasswordResetToken`  

And `TokenUtils` — helper methods for secure random tokens / password hashing helpers.

**Update (Module 10):**  
`AuthController` + `AuthService` now expose `/auth/login`, `/register`, `/refresh`, `/logout`, `/me` for the React SPA.  
Forgot-password HTTP endpoints remain future work; tables/`TokenUtils` stay ready.

---

## 5. Apply the auth tables (hands-on)

If MySQL is ready:

```bash
mysql -h 127.0.0.1 -u root -p hotel_booking \
  < backend/src/main/resources/db/migration/V4__create_auth_tables.sql
```

This creates `users`, `roles`, `user_roles`, `refresh_tokens`, `password_reset_tokens` and seeds ADMIN/CUSTOMER roles.

---

## 6. Try this

1. Read `SecurityConfig` and list every `permitAll` path on paper.  
2. Explain why CSRF is disabled for a JWT API.  
3. Explain why refresh tokens are stored in the DB but access tokens usually are not.  
4. Find where `ROLE_` prefix is added (`CustomUserDetails`).  

---

## 7. Interview warmup

1. Authentication vs authorization?  
2. Why JWT is called “stateless”?  
3. What does the JWT filter do on each request?  
4. `hasRole("ADMIN")` vs `hasAuthority("ROLE_ADMIN")`?  
5. Why BCrypt instead of plain text or MD5?  

---

## 8. What “done” means for Module 3

Done (Module 3 engine):
- Security filter chain  
- JWT utilities  
- User/Role model + migration  
- RBAC rules in config  

Wired later (Module 10 — see [REACT_AUTH.md](REACT_AUTH.md)):
- `POST /auth/register`  
- `POST /auth/login`  
- `POST /auth/refresh`, `POST /auth/logout`, `GET /auth/me`  
- `POST /auth/forgot-password`, `POST /auth/reset-password` (uses `PasswordResetToken` + async email)

Next learning stop after Module 3 historically: [GUESTS.md](GUESTS.md)  
Current path index: [MODULES.md](MODULES.md)  
SPA auth guide: [REACT_AUTH.md](REACT_AUTH.md)
