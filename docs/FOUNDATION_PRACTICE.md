# Foundation Practice — Config, CORS, Maven, Docker, MapStruct

Hands-on exercises for Spring Boot project fundamentals.  
Implemented in this repo with explanations below.

---

## 1. `application-prod.yml` — `ddl-auto: validate` & `show-sql: false`

**File:** `backend/src/main/resources/application-prod.yml`

Activate:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=prod
# or: SPRING_PROFILES_ACTIVE=prod
```

| Setting | Value | What it does |
|---------|-------|----------------|
| `spring.jpa.hibernate.ddl-auto` | `validate` | Hibernate **checks** entity ↔ table match. Does **not** create/alter tables. Safer for production — schema comes from SQL/Flyway migrations. |
| `spring.jpa.show-sql` | `false` | Stops printing every SQL statement to logs (less noise, less risk of leaking data). |
| `server.servlet.context-path` | `/api/v1` | Versions the API base path for prod (see §2). |
| Logging levels | `INFO` / `WARN` | Quieter production logs than `dev`. |

**Contrast with `dev`:** `application-dev.yml` uses `ddl-auto: update` and `show-sql: true` for fast local iteration.

---

## 2. Context path `/api` → `/api/v1` (URL impact)

### How it was implemented

- **Default / dev** (`application.yml`): still `context-path: /api` so local React + Vite proxy keep working.
- **Prod profile** (`application-prod.yml`): `context-path: /api/v1`.
- Nginx example for prod: `docker/nginx-apiv1.conf.example`.

Controllers do **not** change (`@RequestMapping("/guests")` stays). Only the **servlet context prefix** changes.

### URL trace (same controller methods)

| Area | Before (`/api`) | After prod (`/api/v1`) |
|------|-----------------|-------------------------|
| Register | `POST /api/auth/register` | `POST /api/v1/auth/register` |
| Login | `POST /api/auth/login` | `POST /api/v1/auth/login` |
| Me | `GET /api/auth/me` | `GET /api/v1/auth/me` |
| Guests | `/api/guests/**` | `/api/v1/guests/**` |
| Rooms | `/api/rooms/**` | `/api/v1/rooms/**` |
| Bookings | `/api/bookings/**` | `/api/v1/bookings/**` |
| Payments | `/api/payments/**` | `/api/v1/payments/**` |
| Admin reports | `/api/admin/**` | `/api/v1/admin/**` |
| Swagger UI | `/api/swagger-ui.html` | `/api/v1/swagger-ui.html` |
| Actuator health | `/api/actuator/health` | `/api/v1/actuator/health` |

### What else must change when you cut over clients

1. Frontend `VITE_API_BASE_URL` / Axios `baseURL`: `/api` → `/api/v1`
2. Vite proxy key: `/api` → `/api/v1` (or keep proxy path and rewrite)
3. Nginx `location /api/` → `location /api/v1/` (see example file)
4. Any hardcoded Postman/mobile base URLs
5. CORS is origin-based (ports), **not** path-based — CORS list does not need a path change

Security matchers (`/auth/login`, `/admin/**`) stay path-relative to the context — no Java matcher edits required for versioning.

---

## 3. CORS — mobile origin `http://localhost:19006`

**File:** `backend/src/main/java/com/hotelbooking/config/CorsConfig.java`

Allowed origins now:

- `http://localhost:5173` — Vite React
- `http://localhost:3000` — alternate local UI
- `http://localhost:19006` — hypothetical Expo / RN web

**Why:** Browsers block cross-origin XHR/fetch unless the API lists the UI origin. A phone/emulator web bundle on `:19006` is a different origin from `:8080`.

---

## 4. Maven dependency tree — `spring-boot-starter-web`

Captured under [`docs/_maven_web_starter_tree.txt`](_maven_web_starter_tree.txt).

**Paper sketch (what it pulls in):**

```
spring-boot-starter-web
├── spring-boot-starter          (core + logging + YAML)
│   ├── spring-boot
│   ├── spring-boot-autoconfigure
│   ├── spring-boot-starter-logging → Logback + SLF4J bridges
│   └── snakeyaml
├── spring-boot-starter-json     (Jackson Java 8 / JSR-310)
├── spring-boot-starter-tomcat   (embedded Tomcat)
├── spring-web                   (HTTP abstractions)
└── spring-webmvc                (DispatcherServlet, @RestController)
```

Regenerate anytime:

```bash
cd backend && mvn dependency:tree
```

---

## 5. `docker compose config` — each service

Command:

```bash
docker compose config
```

This **resolves** variables/defaults and prints the effective Compose model (does not start containers).

| Service | Role |
|---------|------|
| **mysql** | MySQL 8 image; creates DB `hotel_booking`; port `3306`; volume `mysql-data`; healthcheck via `mysqladmin ping`. |
| **backend** | Builds `docker/Dockerfile.backend` (multi-stage Maven → JRE); depends on healthy MySQL; env `DB_*`, `JWT_SECRET`, `SPRING_PROFILES_ACTIVE=dev`; publishes `8080`. |
| **frontend** | Builds `docker/Dockerfile.frontend` (Nginx + static SPA); depends on backend; publishes `80`; proxies `/api/` to backend (see `docker/nginx.conf`). |

**Volumes:** `mysql-data` persists DB files across restarts.  
**Network:** default bridge so service names (`mysql`, `backend`) resolve as hostnames.

---

## Mini coding tasks

### A. `AppProperties` (`app.name`, `app.version`)

| Piece | Location |
|-------|----------|
| YAML | `application.yml` → `app.name`, `app.version` |
| Binder | `config/AppProperties.java` (`@ConfigurationProperties(prefix = "app")`) |
| Startup log | `config/AppStartupLogger.java` (`ApplicationRunner`) |

On boot you should see:

```text
Application started: name='Hotel Booking System', version='0.0.1-SNAPSHOT', profiles=...
```

### B. Practice `/health` controller — added then removed

Per Module 1 spirit (“no throwaway APIs in the permanent codebase”):

1. Briefly added `PracticeHealthController` → `GET /health` → `{"status":"UP"}`
2. Permitted it in Security
3. **Deleted** the controller and reverted the security matcher

**Use in real apps:** Spring Actuator `GET {context-path}/actuator/health` (already permit-listed).

### C. MapStruct practice mapper

| Type | File |
|------|------|
| Source | `practice/DemoPerson.java` |
| Target | `practice/DemoPersonDto.java` |
| Mapper | `practice/DemoPersonMapper.java` |
| Test | `practice/DemoPersonMapperTest.java` |
| Generated | `backend/target/generated-sources/annotations/com/hotelbooking/practice/DemoPersonMapperImpl.java` |

Verify:

```bash
cd backend
mvn -DskipTests compile
ls target/generated-sources/annotations/com/hotelbooking/practice/DemoPersonMapperImpl.java
mvn test -Dtest=DemoPersonMapperTest
```

The generated class is annotated `@Component` and implements `toDto` / `toEntity` (fullName ↔ first/last).

---

## Multi-stage Docker build (related concept)

See `docker/Dockerfile.backend`:

1. **Stage `build`:** `maven:…-temurin-21` → `mvn package`  
2. **Stage runtime:** `eclipse-temurin:21-jre-alpine` → copy only the JAR  

Result: smaller image, no Maven/source in production.

---

## Quick commands cheat sheet

```bash
# Prod profile (validate + /api/v1)
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Maven web starter tree
cd backend && mvn dependency:tree | less

# Resolved Compose file
docker compose config

# MapStruct generated sources
cd backend && mvn -DskipTests compile && ls target/generated-sources/annotations/com/hotelbooking/practice/
```
