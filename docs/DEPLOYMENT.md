# Module 15 — Production Deployment & DevOps

This guide teaches **how enterprises ship a Spring Boot + React app**, then maps each idea to files in this repo.

---

## Part A — Concepts (study first)

### 1. What makes an app production-ready?

Not “it runs on my laptop.” Production-ready means:

| Concern | Meaning |
|---------|---------|
| **Repeatable builds** | Same commit → same artifact (Docker image / jar / SPA) |
| **Config outside code** | Secrets & env via environment variables, not Git |
| **Observability** | Logs, health checks, metrics you can alert on |
| **Safe schema** | Migrations / `ddl-auto=validate` (no surprise ALTER in prod) |
| **Least privilege** | Non-root containers, private DB network, JWT secrets rotated |
| **Rollback** | Previous image tag can be redeployed quickly |
| **CI gate** | Tests must pass before deploy |

### 2. Why Docker?

Docker packages **app + runtime** so “works on my machine” becomes “works on every machine.”  
You ship an **image**; servers run a **container**. No installing JDK 21 / Node on the host for runtime.

### 3. Why Docker Compose?

Compose starts **multi-container apps** (MySQL + API + Nginx) with one file: networks, volumes, env, healthchecks, restart policies. Perfect for single-VM / lab / small prod. Kubernetes comes later for multi-node scale.

### 4. What Nginx does here

Nginx is the **public door**:

- Serves React static files (`index.html`, `/assets/*`)
- **Reverse-proxies** `/api/*` to Spring Boot
- Adds gzip, cache headers, security headers
- SPA fallback: `try_files … /index.html` for React Router

### 5. Reverse proxy

Browser never talks to Tomcat port 8080 in prod. It talks to Nginx:80. Nginx forwards API paths to `backend:8080`. Benefits: TLS termination, one origin (no CORS pain), hide internal ports.

### 6. CI vs CD

| | CI (Continuous Integration) | CD (Continuous Delivery/Deploy) |
|--|-----------------------------|----------------------------------|
| **When** | Every push/PR | After green CI / on tag |
| **Does** | Build + test | Publish images / deploy |
| **This repo** | `.github/workflows/ci.yml` | `.github/workflows/cd.yml` (template) |

### 7. GitHub Actions

YAML workflows on GitHub runners: checkout → setup JDK/Node → `mvn verify` / `npm test` → optional Docker build. Secrets live in repo Settings → Secrets (never in YAML).

### 8. Environment variables

12-factor rule: config in the environment. Examples: `DB_HOST`, `JWT_SECRET`, `SPRING_PROFILES_ACTIVE`. Compose injects them into containers. See [ENVIRONMENT.md](ENVIRONMENT.md).

### 9. Logging in production

- `INFO` for app, `WARN` for Hibernate SQL (already in `application-prod.yml`)
- Structured logs (JSON) optional later → ELK / CloudWatch
- Never log passwords, full JWTs, or card data

### 10. Health checks

Compose / orchestrators restart unhealthy containers. We probe OpenAPI (`/api/v3/api-docs` or `/api/v1/v3/api-docs`) because Actuator is not on the classpath (pom frozen). Prefer Actuator `/actuator/health` when you can add the dependency later.

### 11. Deployment best practices

1. Immutable images tagged by Git SHA / semver  
2. Secrets in vault / GitHub Secrets / cloud secret manager  
3. DB backups before migrate  
4. Blue/green or rolling when possible  
5. Separate staging from production  

### 12. Common DevOps interview themes

Docker layers & multi-stage builds · Compose vs K8s · reverse proxy · CI gating · secret management · health vs readiness · zero-downtime deploys · observability (logs/metrics/traces).

---

## Part B — How this project deploys

### Deployment flow

```text
Developer  →  GitHub  →  GitHub Actions (CI)
                              ↓
                     Docker build (backend + frontend)
                              ↓
              docker compose (prod) on a VM / server
                              ↓
              Nginx (:80) → React static + proxy /api
                              ↓
                     Spring Boot (:8080 internal)
                              ↓
                          MySQL (:3306 internal)
```

### Runtime request path

```text
Browser
  → Nginx (frontend container)
       ├─ /assets/*, /        → static SPA
       └─ /api/...            → proxy → Spring Boot → MySQL
```

### Profiles & context-path (important)

| Profile | Context path | Nginx config | Frontend `VITE_API_BASE_URL` |
|---------|--------------|--------------|------------------------------|
| default / `dev` | `/api` | `docker/nginx.conf` | `/api` |
| `prod` (`application-prod.yml`) | `/api/v1` | `docker/nginx.prod.conf` | `/api/v1` |

We do **not** change `application.yml` / `pom.xml` in Module 15. Compose chooses profile + Nginx file.

---

## Quick start

### Local (no Docker for app)

See [INSTALLATION.md](INSTALLATION.md).

### Docker development

```bash
cp .env.example .env
docker compose up --build
# UI http://localhost:80   API http://localhost:8080/api
```

### Docker production-shaped

```bash
cp .env.example .env   # set strong DB_PASSWORD + JWT_SECRET
./scripts/deploy.sh prod
# or: docker compose -f docker-compose.prod.yml up --build -d
```

### CI

Push to GitHub → Actions runs `.github/workflows/ci.yml`.

---

## File map

| Path | Role |
|------|------|
| `docker/Dockerfile.backend` | Multi-stage Maven → JRE |
| `docker/Dockerfile.frontend` | Multi-stage Node → Nginx |
| `docker/nginx.conf` | SPA + `/api` proxy |
| `docker/nginx.prod.conf` | SPA + `/api/v1` proxy |
| `docker-compose.yml` | Base / local stack |
| `docker-compose.dev.yml` | Dev port overrides |
| `docker-compose.prod.yml` | Prod profile + locked-down ports |
| `.github/workflows/ci.yml` | Build & test |
| `.github/workflows/cd.yml` | Publish/deploy **template** |
| `scripts/deploy.sh` | One-command up |
| `scripts/backup-mysql.sh` | `mysqldump` via Docker |
| `postman/` | API collection |
| `docs/TLS.md` | TLS termination notes + enable path |
| `docs/BACKUPS.md` | RTO / RPO + restore drill |
| `docker/Dockerfile.backend.distroless` | Optional Distroless runtime |
| `docker-compose.tls.yml` | HTTPS override |

---

## Teaching notes on key Docker instructions

| Instruction | Why |
|-------------|-----|
| `FROM … AS build` | Name a stage; discard compilers later |
| `COPY pom.xml` then `dependency:go-offline` | **Layer cache** — deps reused until pom changes |
| `USER spring` | Non-root runtime |
| `COPY --from=build` | Only the JAR / `dist` enters final image |
| `EXPOSE` | Documentation of ports (Compose still maps) |
| Compose `read_only: true` + `tmpfs:/tmp` | Immutable rootfs; temp only in RAM disk |

---

## Practice solutions (implemented)

| Exercise | Implementation | Flow |
|----------|----------------|------|
| (1) TLS termination notes | [TLS.md](TLS.md), `nginx.tls.conf.example`, `docker-compose.tls.yml` | Browser HTTPS → Nginx decrypts → HTTP to backend |
| (2) Tag images with Git SHA | `.github/workflows/ci.yml` (+ CD push tags) | `github.sha` → `:sha-<7>` + full SHA tags + artifact |
| (3) Restrict Swagger in prod | `application-prod.yml` springdoc off + Nginx 404 | Prod profile disables docs; edge blocks paths |
| (4) Slack notify in CD | `cd.yml` `notify-failure` job | On publish/deploy **failure** → Incoming Webhook |
| (5) RTO/RPO for backups | [BACKUPS.md](BACKUPS.md) | Define loss/downtime targets + mysqldump drill |

| Docker challenge | Implementation | Flow |
|------------------|----------------|------|
| Layer-cache `pom.xml` only | `Dockerfile.backend` two-step COPY | pom layer cached; `src` changes rebuild package only |
| Distroless / JRE size | `Dockerfile.backend.distroless` | Build stage → `gcr.io/distroless/java21` nonroot (no shell) |
| Read-only rootfs | `docker-compose.prod.yml` `read_only` + `tmpfs` | Root FS immutable; `/tmp` (and nginx cache) writable |

---

*Module 15 — Production Deployment*
