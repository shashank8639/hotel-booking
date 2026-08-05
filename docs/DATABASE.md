# Module 2 Guide — Database Design (Learn the Foundation)

Before services and APIs, we designed the **data model**. Seniors do this first because every feature later depends on it.

**Mentor question to keep asking:**  
*“If I delete this row, what else breaks? If I change this price, should old bookings change too?”*

Those questions led to foreign keys, `ON DELETE RESTRICT`/`CASCADE`, and the price snapshot on `booking_rooms`.

**How to study this module:**
1. Sketch the ER diagram from memory  
2. Open each entity and match fields to SQL columns  
3. Apply V1 (and V2/V3 if needed) on MySQL and run `SHOW TABLES`  
4. Only then move to Security (Module 3) and Guest APIs (Module 4)

Learning path: [MODULES.md](MODULES.md)

---

## What you built (checklist)

| Item | Status |
|------|--------|
| SQL schema (`V1__create_tables.sql`) | Done |
| Sample seed data (`V2__seed_sample_data.sql`) | Done |
| Optimistic locking + index (`V3`) | Done |
| JPA entities (5 core booking entities) | Done |
| Spring Data repositories | Done |
| Flyway dependency auto-run in `pom.xml` | Pending (later) |

## Entity Relationship Diagram

```
    ┌──────────┐         ┌──────────┐         ┌──────────────┐         ┌──────────┐
    │  GUEST   │ 1     N │ BOOKING  │ 1     N │ BOOKING_ROOM │ N     1 │   ROOM   │
    │──────────│─────────│──────────│─────────│──────────────│─────────│──────────│
    │ PK id    │         │ PK id    │         │ PK id        │         │ PK id    │
    │ email UK │         │ FK guest │         │ FK booking   │         │ room_no  │
    └──────────┘         │ dates    │         │ FK room      │         └──────────┘
                         │ status   │         │ price snap   │
                         └────┬─────┘         └──────────────┘
                              │ 1
                              │ N
                         ┌────▼─────┐
                         │ PAYMENT  │
                         │──────────│
                         │ PK id    │
                         │ FK book  │
                         │ amount   │
                         └──────────┘
```

## Tables

| Table | Description | Key constraints |
|-------|-------------|-----------------|
| `guests` | Hotel customer profiles | Unique `email` |
| `rooms` | Physical room inventory | Unique `room_number` |
| `bookings` | Guest reservations | FK → `guests`, check-out > check-in |
| `booking_rooms` | Rooms assigned to a booking | FK → `bookings`, `rooms`; unique (booking, room) |
| `payments` | Payment transactions | FK → `bookings`; unique `transaction_reference` |

## JPA Entities

| Entity | Table | Package |
|--------|-------|---------|
| `Guest` | `guests` | `com.hotelbooking.entity` |
| `Room` | `rooms` | `com.hotelbooking.entity` |
| `Booking` | `bookings` | `com.hotelbooking.entity` |
| `BookingRoom` | `booking_rooms` | `com.hotelbooking.entity` |
| `Payment` | `payments` | `com.hotelbooking.entity` |

Domain enums (`BookingStatus`, `RoomStatus`, `RoomType`, `PaymentStatus`, `PaymentMethod`) live in `com.hotelbooking.database`.

## Relationships

| From | To | Type | Owning side | Cascade | Fetch |
|------|----|------|-------------|---------|-------|
| Guest | Booking | 1:N | Booking (`guest_id`) | None | LAZY |
| Booking | BookingRoom | 1:N | BookingRoom (`booking_id`) | ALL | LAZY |
| Room | BookingRoom | 1:N | BookingRoom (`room_id`) | None | LAZY |
| Booking | Payment | 1:N | Payment (`booking_id`) | ALL | LAZY |

## Repositories

| Repository | Key query methods |
|------------|-------------------|
| `GuestRepository` | `findByEmail`, `existsByEmail` |
| `RoomRepository` | `findByRoomNumber`, `findByStatus`, `findByRoomType` |
| `BookingRepository` | `findByGuestId`, `findByStatus`, `findByCheckInDateBetween` |
| `BookingRoomRepository` | `findByBookingId`, `findByRoomId` |
| `PaymentRepository` | `findByBookingId`, `findByStatus`, `findByTransactionReference` |

## Applying the Schema

### Option A — Manual (MySQL CLI)

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS hotel_booking;"
mysql -u root -p hotel_booking < backend/src/main/resources/db/migration/V1__create_tables.sql
mysql -u root -p hotel_booking < backend/src/main/resources/db/migration/V2__seed_sample_data.sql
```

### Option B — Docker Compose (MySQL only)

Start MySQL, then apply migrations:

```bash
docker compose up -d mysql
# wait for healthy, then run Option A commands against localhost:3306
```

### Option C — Tests (automatic)

Tests use H2 in-memory with `ddl-auto: create-drop` — no MySQL required for `mvn test`.

## Migration Files

| File | Purpose |
|------|---------|
| `db/migration/V1__create_tables.sql` | Full schema with PKs, FKs, indexes, check constraints |
| `db/migration/V2__seed_sample_data.sql` | Optional dev sample data (3 guests, 5 rooms, 2 bookings) |
| `db/migration/SCHEMA_REFERENCE.sql` | Documentation pointer |

## Design Decisions

1. **BigDecimal** for all monetary columns — avoids floating-point errors.
2. **LocalDate** for check-in/check-out — date-only semantics, no timezone bugs.
3. **Price snapshot** in `booking_rooms` — preserves price at booking time even if room rates change.
4. **ON DELETE RESTRICT** on `guests` — prevents accidental loss of booking history.
5. **ON DELETE CASCADE** on `booking_rooms` and `payments` — line items and payments tied to booking lifecycle.
6. **FetchType.LAZY** on all relationships — performance best practice; explicit fetch joins in services when needed.

## Related Modules

- **Module 3** — Auth tables (`users`, `roles`, `refresh_tokens`, …) in `V4__create_auth_tables.sql` — see [SECURITY.md](SECURITY.md)
- **Module 4** — Guest Management APIs on top of `guests` — see [GUESTS.md](GUESTS.md)
- **Module 5 (next)** — Room Management APIs on top of `rooms`
