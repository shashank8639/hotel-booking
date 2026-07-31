# Security & Authentication — Module 3

## Status

| Item | Status |
|------|--------|
| User, Role, RefreshToken, PasswordResetToken entities | Done |
| Auth repositories | Done |
| JWT service + filter + entry point | Done |
| SecurityConfig (RBAC, stateless) | Done |
| Swagger JWT bearer scheme | Done (Module 1 OpenApiConfig) |
| Flyway V4 auth tables | Done |
| Auth REST endpoints (login/register) | Module 4 |
| Auth services | Module 4 |

## Apply Migration

```bash
mysql -h 127.0.0.1 -u root -p hotel_booking \
  < backend/src/main/resources/db/migration/V4__create_auth_tables.sql
```

## Roles

| Role | Spring Authority | Access |
|------|------------------|--------|
| ADMIN | `ROLE_ADMIN` | `/admin/**`, `/payments/**`, all authenticated routes |
| CUSTOMER | `ROLE_CUSTOMER` | `/bookings/**`, authenticated routes |

Public: `/auth/**`, `GET /rooms/**`, Swagger.

## Key Classes

| Class | Package | Purpose |
|-------|---------|---------|
| `SecurityConfig` | config | Filter chain, RBAC rules |
| `JwtAuthenticationFilter` | security | Validates Bearer JWT per request |
| `JwtService` | security | Create/validate tokens |
| `CustomUserDetailsService` | security | Load user for authentication |
| `User` | entity | Login account |
| `RefreshToken` | entity | Server-side refresh token store |

See [MODULES.md](MODULES.md) for full learning path.
