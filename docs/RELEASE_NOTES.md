# Release Notes — Module 15 (Production Deployment & DevOps)

## Summary

Module 15 prepares the Hotel Booking System for production-shaped deployment **without changing business logic, `pom.xml`, or `application.yml`**.

## Added

- Multi-stage `docker/Dockerfile.backend` and `docker/Dockerfile.frontend`
- Hardened `docker/nginx.conf` + `docker/nginx.prod.conf` (`/api/v1`)
- `docker-compose.yml`, `docker-compose.dev.yml`, `docker-compose.prod.yml`
- GitHub Actions `ci.yml` (build/test/image smoke) and `cd.yml` (publish/deploy template)
- `scripts/deploy.sh`, `scripts/backup-mysql.sh`
- Docs: Deployment, Installation, Environment, Swagger, Production checklist
- Postman collection + environment
- Screenshot placeholders under `docs/screenshots/`

## Practice stretch (follow-up)

- TLS termination guide + `docker-compose.tls.yml`
- CI/CD image tags include full Git SHA + `sha-<short>`
- Prod Swagger disabled (springdoc + Nginx)
- Slack failure notify wired in CD (`SLACK_WEBHOOK_URL`)
- RTO/RPO documented in `docs/BACKUPS.md`
- Docker challenges: pom-only layer cache, Distroless Dockerfile, read-only rootfs in prod Compose

## Notes

- Prod Spring profile already sets `context-path=/api/v1` — Compose prod aligns Nginx + Vite base URL
- Prod healthcheck no longer uses OpenAPI (Swagger off); probes HTTP from `/auth/login`
- CD publish/deploy jobs stay gated `if: false` until registry/SSH secrets are configured; Slack job runs when those jobs fail

## Upgrade path for operators

1. Pull Module 15 files  
2. `cp .env.example .env` and set secrets  
3. `./scripts/deploy.sh prod` on a Docker host  
4. Apply any SQL migrations beyond what init scripts load  
