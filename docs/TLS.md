# TLS termination with Nginx (notes + example)

## What “TLS termination” means

Browsers speak **HTTPS** to Nginx. Nginx decrypts TLS, then talks **plain HTTP** to Spring Boot on the private Docker network (`backend:8080`).

```text
Internet (HTTPS :443)
        ↓
   Nginx (terminates TLS — holds cert + private key)
        ↓ HTTP
   Spring Boot :8080  (never exposed publicly)
```

You do **not** need Tomcat SSL unless you do end-to-end encryption inside the mesh (unusual for this Compose layout).

---

## Where certificates live

| Approach | Certs | When |
|----------|-------|------|
| **Nginx in container** | Mount `fullchain.pem` + `privkey.pem` read-only | Single VM / lab |
| **Cloud load balancer** | AWS ALB / Cloudflare / Azure App Gateway | Real prod — LB terminates TLS, Nginx may stay HTTP internally |
| **Caddy / Traefik** | Auto Let’s Encrypt | Small teams wanting automatic renewals |

Never bake private keys into the Docker **image**. Mount them at runtime or use a secret store.

---

## Example: Nginx HTTPS server block

File: `docker/nginx.tls.conf.example` (not used by default Compose — enable when you have certs).

Flow to enable on a VM:

1. Obtain certs (Let’s Encrypt `certbot` or cloud ACM).  
2. Place files on host, e.g. `/etc/hotel-booking/certs/`.  
3. Mount into frontend container and point `default.conf` at the TLS example.  
4. Publish host `443:443` (and optionally redirect `80` → `443`).  
5. Keep `X-Forwarded-Proto` so Spring can generate correct absolute URLs if needed.

```bash
# Host has certs at ./certs/fullchain.pem and ./certs/privkey.pem
docker compose -f docker-compose.prod.yml -f docker-compose.tls.yml up -d
```

(`docker-compose.tls.yml` is a thin override — see that file.)

---

## Security headers with HTTPS

After TLS is on, enable HSTS in Nginx (only when HTTPS is real):

```nginx
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
```

Do **not** enable HSTS while still serving plain HTTP on the public hostname.

---

## Common mistakes

| Mistake | Result |
|---------|--------|
| Expose Spring `:8080` with TLS only on Nginx | Users can bypass TLS if 8080 is public |
| Commit `privkey.pem` to Git | Key leak — revoke & reissue |
| Wrong `X-Forwarded-Proto` | Mixed-content / wrong redirect URLs |
| Cert path not mounted | Nginx fails to start |

---

## Interview one-liner

*“TLS terminates at the edge (Nginx or cloud LB). The app container stays on HTTP inside a private network; certificates are mounted as secrets, not baked into images.”*
