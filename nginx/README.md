# Nginx configs live under `docker/` (Compose build context).
#
# | File | When to use |
# |------|-------------|
# | `docker/nginx.conf` | Default `/api` context-path (dev / default profile) |
# | `docker/nginx.prod.conf` | `SPRING_PROFILES_ACTIVE=prod` → `/api/v1` |
# | `docker/nginx-apiv1.conf.example` | Legacy example (same idea as nginx.prod.conf) |
#
# See [docs/DEPLOYMENT.md](../docs/DEPLOYMENT.md).
