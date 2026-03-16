# Installation Guide

## Prerequisites

| Tool | Version |
|------|---------|
| JDK | 21 |
| Maven | 3.9+ |
| Node.js | 20+ |
| npm | 10+ (comes with Node) |
| MySQL | 8.x (or Docker) |
| Docker Desktop / Engine | 24+ (optional but recommended) |
| Docker Compose | v2 (`docker compose`) |

Verify:

```bash
java -version
mvn -v
node -v
docker compose version
```

---

## Option A — Local processes (developer laptop)

### 1. Database

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS hotel_booking;"
mysql -u root -p hotel_booking < backend/src/main/resources/db/migration/V1__create_tables.sql
# Apply later migrations as needed (V3…V10) — see docs/DATABASE.md
mysql -u root -p hotel_booking < backend/src/main/resources/db/migration/V2__seed_sample_data.sql
```

Backend uses `ddl-auto: validate` — tables must already exist.

### 2. Backend

```bash
cd backend
# Optional: export DB_PASSWORD=... JWT_SECRET=...
mvn spring-boot:run
```

- API: `http://localhost:8080/api`
- Swagger: `http://localhost:8080/api/swagger-ui.html`

### 3. Frontend

```bash
cd frontend
cp .env.example .env   # if present under frontend; else set VITE_API_BASE_URL
# Root .env.example documents VITE_API_BASE_URL=/api for Docker;
# for Vite dev, frontend often uses proxy — see vite.config.js
npm ci
npm run dev
```

- App: `http://localhost:5173`
- Vite proxies `/api` → `http://localhost:8080`

---

## Option B — Docker Compose (full stack)

```bash
cd /path/to/hotel-booking
cp .env.example .env
docker compose up --build
```

| Service | URL |
|---------|-----|
| Website | http://localhost:80 |
| API (direct) | http://localhost:8080/api |
| Swagger | http://localhost:8080/api/swagger-ui.html |

First MySQL boot mounts `V1` (+ `V2` seed) via `docker-entrypoint-initdb.d`.  
Later SQL files (`V3`+) must be applied manually or via your migration process.

Stop:

```bash
docker compose down
# keep data: omit -v
# wipe DB volume: docker compose down -v
```

---

## Option C — Production-shaped Compose

```bash
cp .env.example .env
# Set strong DB_PASSWORD and JWT_SECRET
./scripts/deploy.sh prod
```

Uses `SPRING_PROFILES_ACTIVE=prod` → API under **`/api/v1`**.  
Frontend is built with `VITE_API_BASE_URL=/api/v1` and Nginx `nginx.prod.conf`.

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Backend fails on startup with schema validation | Apply SQL migrations before start |
| Frontend 404 on refresh of `/rooms` | Use Nginx `try_files` (Docker) or Vite dev server |
| CORS errors | Prefer same-origin `/api` via Nginx; or check `CorsConfig` |
| JWT 401 | Ensure `JWT_SECRET` matches across restarts; use ADMIN/CUSTOMER tokens from `/auth/login` |
| MySQL healthcheck fails | Wait for `start_period`; check password special characters |

More: [DEPLOYMENT.md](DEPLOYMENT.md), [ENVIRONMENT.md](ENVIRONMENT.md).
