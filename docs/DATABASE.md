# Database Guide — Schema & Migrations

Before services and APIs, we designed the **data model**. Seniors do this first because every feature later depends on it.

**Mentor question to keep asking:**  
*“If I delete this row, what else breaks? If I change this price, should old bookings change too?”*

Those questions led to foreign keys, `ON DELETE RESTRICT`/`CASCADE`, and the price snapshot on `booking_rooms`.

Learning path: [MODULES.md](MODULES.md) · Multi-hotel: [MULTI_HOTEL.md](MULTI_HOTEL.md)

---

## What you built (checklist)

| Item | Status |
|------|--------|
| Core booking schema (`V1`) + seed (`V2`) | Done |
| Optimistic locking + indexes (`V3`) | Done |
| Auth tables + roles (`V4`) | Done |
| Room enhancements (`V5`, `V7`) | Done |
| Payments (`V6`, `V9`) | Done |
| Booking stretch / holds (`V8`) | Done |
| Email outbox (`V10`) | Done |
| Report index example (`V11*.example`) | Optional |
| **Multi-hotel geo + hotels (`V12`)** | Done |
| JPA entities + Spring Data repositories | Done |

Apply SQL files **in version order** against MySQL 8 with `utf8mb4_unicode_ci` (see root [README.md](../README.md)).

---

## Core tables (Modules 1–8)

| Table | Purpose |
|-------|---------|
| `guests` | Booking-party profiles (separate from login `users`) |
| `rooms` | Sellable inventory; linked to `hotels` after V12 |
| `bookings` / `booking_rooms` | Reservations + price snapshots |
| `payments` | Razorpay orders / captures / refunds |
| `users` / `roles` / `user_roles` | JWT principals + RBAC |

## Multi-hotel tables (Module 16 / V12)

| Table | Purpose |
|-------|---------|
| `countries` / `states` / `cities` | Geo hierarchy (Telangana seeded) |
| `hotels` | Catalog properties (APPROVED / PENDING / …) |
| `hotel_images` / amenities / policies / reviews | Hotel merchandising |
| `rooms.hotel_id` | Room belongs to one hotel; unique `(hotel_id, room_number)` |

---

## Local apply (summary)

```bash
mysql -h 127.0.0.1 -u root -p -e \
  "CREATE DATABASE IF NOT EXISTS hotel_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Then run V1…V10 and V12 from backend/src/main/resources/db/migration/
```

Quote passwords that contain `$`. Prefer the migration loop documented in the root README.

---

## Study tips

1. Sketch Guest → Booking → BookingRoom → Room → Hotel → City  
2. Open entities under `com.hotelbooking.entity` and match columns  
3. Trace overlap query in `BookingRepository.existsOverlappingBooking`  
4. Trace hotel search in `HotelRepository.searchPublic` (collation-safe `LIKE`)

*Last updated: Module 16 — multi-hotel schema*
