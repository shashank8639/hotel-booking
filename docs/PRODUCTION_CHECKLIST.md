# Production Go-Live Checklist

## Security

- [ ] Strong unique `JWT_SECRET` (not the example string) — **enforced** by `ProductionSafetyRunner` on `prod`
- [ ] Strong `DB_PASSWORD`; DB not published on `0.0.0.0:3306`
- [ ] HTTPS terminated (load balancer / Nginx TLS) before public traffic
- [ ] CORS limited to real front-end origins (`app.cors.allowed-origins` / `APP_CORS_ORIGIN`)
- [ ] Swagger restricted or disabled on public prod (`application-prod.yml`)
- [ ] Razorpay live keys only in secret store; mock disabled — **enforced** on `prod`
- [ ] Mail SMTP credentials in secret store
- [ ] Bootstrap admin via `APP_BOOTSTRAP_ADMIN_*` once, then remove password from env
- [ ] Paid bookings cannot be cancelled until refunded (API rule)

## Data

- [ ] Schema migrations applied (V1…current) on empty prod DB
- [ ] `ddl-auto=validate` (prod profile)
- [ ] Backup job scheduled (`scripts/backup-mysql.sh` or managed backup)
- [ ] Restore tested once

## Runtime

- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] Frontend built with matching `VITE_API_BASE_URL` (`/api/v1`)
- [ ] Nginx uses `nginx.prod.conf` for `/api/v1`
- [ ] Healthchecks green in Compose / orchestrator
- [ ] Resource limits set (CPU/memory) for containers
- [ ] Log aggregation destination chosen

## CI/CD

- [ ] CI green on release commit
- [ ] Images tagged with **Git SHA** (and optional semver)
- [ ] CD secrets configured; template jobs enabled intentionally
- [ ] `SLACK_WEBHOOK_URL` set for failure alerts
- [ ] Rollback procedure documented (previous SHA tag)

## Ops

- [ ] On-call knows how to read logs (`docker compose logs -f backend`)
- [ ] Runbook: payment webhook failures, DB disk full, certificate expiry
- [ ] RPO/RTO agreed — see [BACKUPS.md](BACKUPS.md); restore drill timed once
- [ ] TLS plan documented — see [TLS.md](TLS.md)
- [ ] Swagger confirmed disabled on public prod

## Sign-off

| Role | Name | Date |
|------|------|------|
| Dev lead | | |
| Ops / DevOps | | |
