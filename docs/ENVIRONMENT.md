# Environment Variables

Config belongs in the **environment**, not in Git.  
Copy `.env.example` → `.env` for Compose. Spring Boot also reads OS env / IDE run configs.

---

## Database

| Variable | Used by | Default / notes |
|----------|---------|-----------------|
| `DB_HOST` | Backend | `localhost` locally; `mysql` in Compose |
| `DB_PORT` | Backend / Compose | `3306` |
| `DB_NAME` | Backend / MySQL | `hotel_booking` |
| `DB_USERNAME` | Backend | `root` (prefer least-privilege user in real prod) |
| `DB_PASSWORD` | Backend / MySQL | **Required strong secret in prod** |

JDBC URL is built in `application.yml` / `application-prod.yml` from these values.

---

## Spring / server

| Variable | Meaning |
|----------|---------|
| `SPRING_PROFILES_ACTIVE` | `dev` / `prod` / … — Compose prod forces `prod` |
| `SERVER_PORT` | Host port mapped to container `8080` (dev compose) |
| `APP_VERSION` | Shown in prod app metadata |

**Do not commit** production profile secrets. `application-prod.yml` only adjusts safe defaults (logging, `ddl-auto=validate`, context-path `/api/v1`).

---

## Security

| Variable | Meaning |
|----------|---------|
| `JWT_SECRET` | HMAC key for JWT — **≥256-bit random**, unique per environment |
| `JWT_EXPIRATION_MS` | Access token TTL (if used by config) |

---

## Frontend (build-time)

| Variable | Meaning |
|----------|---------|
| `VITE_API_BASE_URL` | Axios base URL baked by Vite. Prefer `/api` or `/api/v1` (same origin via Nginx). Absolute `http://host:8080/api` only for special local cases. |

Vite embeds `VITE_*` at **`npm run build` / Docker build** time. Changing Compose env after the image is built does **not** change the SPA until rebuild.

---

## Mail / payments (optional)

Documented in Module 7–8 guides. Typical env-style keys (bound via `@ConfigurationProperties`):

| Area | Examples |
|------|----------|
| Mail | `APP_MAIL_ENABLED`, `APP_MAIL_HOST`, `APP_MAIL_USERNAME`, `APP_MAIL_PASSWORD` |
| Razorpay | `APP_RAZORPAY_MOCK_ENABLED`, key id/secret via Razorpay properties |

---

## Compose-only

| Variable | Meaning |
|----------|---------|
| `FRONTEND_PORT` | Host port → Nginx 80 |
| `IMAGE_TAG` | Tag for prod images |
| `JAVA_TOOL_OPTIONS` | JVM flags (RAM percentage, etc.) |

---

## Secrets management (enterprise)

1. **Local:** `.env` (gitignored)  
2. **CI:** GitHub Actions Secrets  
3. **Cloud:** AWS Secrets Manager / SSM, Azure Key Vault, GCP Secret Manager  
4. Rotate `JWT_SECRET` and DB passwords on a schedule; invalidate sessions after JWT rotation  

Never paste secrets into Dockerfiles, README screenshots, or chat logs.
