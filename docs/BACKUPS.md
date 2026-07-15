# Backup objectives — RTO / RPO

## Definitions

| Term | Meaning | Question it answers |
|------|---------|---------------------|
| **RPO** (Recovery Point Objective) | Max **data loss** you can tolerate, measured in time | “How old can the restore be?” |
| **RTO** (Recovery Time Objective) | Max **downtime** to restore service | “How fast must we be back?” |

Example targets for this training app (adjust for a real hotel):

| Environment | RPO | RTO | How we meet it |
|-------------|-----|-----|----------------|
| Lab / demo | 24h | 4h | Nightly `scripts/backup-mysql.sh` + rebuild Compose |
| Staging | 12h | 2h | Scheduled dump to object storage |
| Production (suggested) | ≤ 1h | ≤ 1h | Automated dumps every hour **or** managed DB continuous backup + documented restore drill |

---

## What we back up

- **MySQL logical dump** (`mysqldump --single-transaction`) via `scripts/backup-mysql.sh`  
- Optional: Compose project files + `.env` in a **separate** secrets vault (not in the SQL dump)

We do **not** treat raw copies of `/var/lib/mysql` while MySQL is running as a consistent backup.

---

## Flow (logical backup)

```text
Cron / operator
    → scripts/backup-mysql.sh
    → docker exec mysqldump --single-transaction
    → gzip → ./backups/hotel_booking_<timestamp>.sql.gz
    → (prod) copy to S3 / GCS / off-site
```

Restore drill (prove RTO):

```bash
gunzip -c backups/hotel_booking_YYYYMMDD.sql.gz \
  | docker exec -i hotel-booking-mysql mysql -uroot -p"$DB_PASSWORD" hotel_booking
# then: docker compose -f docker-compose.prod.yml up -d
# smoke: login + one booking read
```

Time the restore — that measurement **is** your real RTO.

---

## Consistency

`--single-transaction` (InnoDB) gives a point-in-time consistent snapshot without long global locks.  
App traffic can continue during the dump; in-flight commits after the snapshot start are simply “after the RPO point.”

---

## Interview one-liner

*“RPO is how much data we can lose; RTO is how fast we recover. We meet them with scheduled consistent mysqldump, off-site copies, and a practiced restore — not just ‘we have a backup file somewhere.’”*
