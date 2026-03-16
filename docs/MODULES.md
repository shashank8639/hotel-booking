# Your Learning Path — Hotel Booking System

Welcome. This is a guided path to become capable of building enterprise Java full-stack apps on your own.

---

## Where you are now

| Module | What you built | Guide |
|--------|----------------|-------|
| 1–15 | Single-hotel product through deployment | See docs map |
| **16** | **Multi-hotel platform (Telangana)** | [MULTI_HOTEL.md](MULTI_HOTEL.md) ← **you are here** |

---

## Module 16 — Multi-Hotel Platform (current)

**Status:** Foundation shipped  

**What changed:** Country → State → City → Hotel → Room. Public hotel search, featured hotels, owner submit + admin approval APIs, landing redesigned for Telangana destinations. Existing Grand Horizon rooms backfilled under Hyderabad.

**Apply migration:**
```bash
mysql -h 127.0.0.1 -u root -p hotel_booking \
  < backend/src/main/resources/db/migration/V12__multi_hotel_platform.sql
```

**Try:** open `/` → search Hyderabad → hotel detail → rooms → existing book/pay.

---

## Documentation map

| Topic | Doc |
|-------|-----|
| Learning order | This file |
| **Multi-hotel** | [MULTI_HOTEL.md](MULTI_HOTEL.md) |
| Deployment | [DEPLOYMENT.md](DEPLOYMENT.md) |
| Testing | [TESTING.md](TESTING.md) |
| Architecture | [ARCHITECTURE.md](ARCHITECTURE.md) |

---

*Last updated: Module 16 foundation — Multi-Hotel Telangana platform*

