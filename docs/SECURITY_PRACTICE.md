# Security Practice — PreAuthorize, 403 JSON, JWT claims, BCrypt, RBAC

Companion drills for Module 3. Code lives under `security/`, `controller/PracticeSecurityController`, and tests in `src/test/.../security/`.

---

## 1. `@PreAuthorize` + AccessDeniedHandler (403 JSON)

### What was added

| Piece | Role |
|-------|------|
| `AdminOnlyDemoService` | `@PreAuthorize("hasRole('ADMIN')")` on `adminOnlyPing()` |
| `PracticeSecurityController` | `GET /practice/security/admin-ping` → calls that method |
| `JwtAccessDeniedHandler` | Writes **403** JSON (`status`, `error`, `message`, `path`) |
| `SecurityConfig` | `.accessDeniedHandler(jwtAccessDeniedHandler)` next to the 401 entry point |

### Flow

```
CUSTOMER JWT → URL /practice/security/** allowed (authenticated)
            → Method @PreAuthorize(hasRole ADMIN) fails
            → AccessDeniedException
            → JwtAccessDeniedHandler → 403 JSON
```

### Try it

```bash
# Login as customer, then:
curl -i -H "Authorization: Bearer <customer-access>" \
  http://localhost:8080/api/practice/security/admin-ping
# → 403 {"error":"Forbidden","message":"Access denied",...}
```

### Method security vs filter 403

- **URL rules** (`authorizeHttpRequests`) → `JwtAccessDeniedHandler` (servlet filter)
- **`@PreAuthorize` on controller/service** → exception reaches `@RestControllerAdvice`; handled by `GlobalExceptionHandler#handleAccessDenied` → same **403** + `"Access denied"` message

Both paths return Forbidden JSON; do not let `AccessDeniedException` fall through to the generic 500 handler.


## 2. Tracing the filter chain (debug logging)

### Config (`application-dev.yml`)

```yaml
logging.level.org.springframework.security: DEBUG
logging.level.com.hotelbooking.security.SecurityFilterTraceFilter: DEBUG
```

### Order (simplified)

```
Incoming request
  → SecurityContextPersistence / Stateless context
  → CorsFilter
  → JwtAuthenticationFilter          (Bearer → SecurityContext)
  → SecurityFilterTraceFilter        (DEBUG log principal + authorities)
  → UsernamePasswordAuthenticationFilter (unused for JWT login body)
  → ExceptionTranslationFilter       (401 / 403 handlers)
  → AuthorizationFilter              (authorizeHttpRequests)
  → Controller → @PreAuthorize (method security)
```

`SecurityFilterTraceFilter` is registered with `addFilterAfter(..., JwtAuthenticationFilter.class)` so you see the principal **after** JWT parsing.

---

## 3. JWT drills

| Drill | Implementation |
|-------|----------------|
| Expired token fails | `JwtServiceTest#expiredToken_shouldNotBeValid` (`expirationMs=1`, sleep, assert expired + invalid) |
| Extract roles | `JwtService#extractRoles` + test expects `ROLE_CUSTOMER` |
| Access vs refresh | `JwtService#extractTokenType` + `isAccessToken` / `isRefreshToken` (`type` claim) |

Claim names: `roles`, `type` (`ACCESS` \| `REFRESH`) — see `SecurityConstants`.

---

## 4. BCrypt — same password, different hashes

`BCryptPasswordEncoderTest`:

1. `encode("password123")` twice → **two different** hash strings (random salt)
2. `matches(raw, hash1)` and `matches(raw, hash2)` both **true**
3. Wrong password → **false**

---

## 5. RBAC matrix (URL + method)

### URL layer (`SecurityConfig`)

| Pattern | Who |
|---------|-----|
| `/auth/register`, `/login`, `/refresh`, forgot/reset | Public |
| `GET /rooms/**` | Public |
| `POST /payments/webhook` | Public (signature verified in service) |
| `/admin/**` | **ADMIN** |
| `/payments/**` | ADMIN or CUSTOMER |
| `/bookings/**` | ADMIN or CUSTOMER (coarse gate) |
| `/practice/security/**` | Authenticated |
| everything else | Authenticated |

### Method layer (finer booking rules)

| Endpoint | Rule |
|----------|------|
| `GET /bookings/{id}` | ADMIN **or** `@bookingOwnership.canAccess(#id)` |
| `PUT /bookings/{id}/cancel` | ADMIN **or** ownership |
| `GET /bookings` (list all) | **ADMIN only** |
| `PUT /bookings/{id}/status` | `@bookingOwnership.canModifyStatus` → **ADMIN only** |
| `POST /bookings`, availability, by guest/status | Still role-gated at URL; ownership can be tightened later |

### Ownership design (`BookingOwnership` bean)

```
ADMIN     → always canAccess
CUSTOMER  → booking.guest.email equals authenticated username (email)
```

Until a formal `User` ↔ `Guest` FK exists, **guest email matching login email** is the ownership key.  
Wire in SpEL: `@PreAuthorize("hasRole('ADMIN') or @bookingOwnership.canAccess(#id)")`.

Future hardening:
- Persist `guest.userId` or `user.guestId`
- Customers create bookings only for their linked guest
- List endpoints filter by owner in the query (not only by id check)

---

## Commands

```bash
cd backend
mvn test -Dtest=JwtServiceTest,BCryptPasswordEncoderTest,PreAuthorizeAccessDeniedIntegrationTest,SecurityIntegrationTest
```
