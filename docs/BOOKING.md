# Module 6 Guide — Booking Engine (Learn by Building)

This is the **core business module**. Guest and Room were inventory of *who* and *what*. Booking is *when* they connect — with money, dates, and concurrency.

---

## 1. What problem are we solving?

A hotel must:

- Reserve rooms for a guest between check-in and check-out  
- Never double-book the same room for overlapping nights  
- Snapshot prices at booking time (rates change later)  
- Move a reservation through a clear lifecycle  
- Cancel safely when rules allow  

That is the **Booking Engine**.

---

## 2. Mental model

```
Guest (who)          Room (what)           Booking (when + money)
    │                    │                        │
    └──────────► Booking ◄────── BookingRoom ─────┘
                 (header)         (line items +
                                  price snapshot)
```

**Why BookingRoom exists:** a booking can include multiple rooms, each with its own nightly rate snapshot, nights, and subtotal. A plain `@ManyToMany` cannot store that money metadata.

---

## 3. Guided tour (open in this order)

### A. Domain rules
- `util/BookingDateUtils.java` — nights, past dates, order  
- `database/BookingStatus.java` — PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT (+ CANCELLED)  

### B. Persistence
- `entity/Booking.java` — header + `@Version` optimistic lock + `@SQLRestriction` (hide CANCELLED from default queries) + composite index `(guest_id, status)`  
- `entity/BookingRoom.java` — junction + price snapshot  
- `repository/BookingRepository.java` — **overlap query** (`findOverlappingBookings`)  
- `repository/RoomRepository.java` — `findByIdForUpdate` (pessimistic lock)  
- `entity/Payment.java` — `paidAt` is `@Column(updatable = false)` (stamp via native UPDATE once)  

**Why `@Version` for hotels:** front desk and online/OTA channels often update the same booking (status, dates, notes). Without optimistic locking, the last write silently wins and you can lose a check-in or cancel. With `@Version`, concurrent updates get a **409** so staff can refresh and retry.

**When to use `@SQLRestriction`:** soft-hide rows that should not appear in the normal “active inventory” view (here: cancelled stays). Use it when most screens should ignore those rows automatically. Do **not** rely on it alone when admin reports or history must still count/list cancelled bookings — bypass with native SQL (as in `ReportQueryRepository` / `findCancelledBookings`).

### C. Service brain
Open `service/impl/BookingServiceImpl.java` and find:
1. Date validation + nights  
2. Duplicate room ID rejection  
3. Guest / room existence  
4. Room must be `AVAILABLE`  
5. Overlap check per room  
6. Price snapshot = `room.getEffectivePrice()`  
7. Total = Σ (price × nights)  
8. Status transition map  
9. Cancel rules  

### D. API
- `controller/BookingController.java` — `/bookings/**` (ADMIN or CUSTOMER per SecurityConfig)

---

## 4. Overlap rule (memorize this)

Two date ranges overlap when:

```
existing.checkIn  <  request.checkOut
AND
existing.checkOut >  request.checkIn
```

Adjacent stays are OK: check-out day == next check-in (no shared night).

Cancelled bookings are **excluded** so inventory can be reused.

---

## 5. Concurrency (interview gold)

| Layer | What we do |
|-------|------------|
| Business | Overlap query before save |
| Transaction | `@Transactional` on create — all rooms or rollback |
| Lock | `findByIdForUpdate` — `SELECT … FOR UPDATE` on room rows |
| Optimistic | `@Version` on Booking for concurrent status updates |
| HTTP | `OptimisticLockingFailureException` → 409 |

Race without locks: two requests both pass “available”, both insert → double booking.

---

## 6. Follow one create request

```
POST /api/bookings + JWT (ADMIN|CUSTOMER)
  → SecurityConfig
  → BookingController (@Valid)
  → BookingServiceImpl
       validate dates / nights
       load guest
       for each room: FOR UPDATE → status → overlap → snapshot line
       totalAmount = sum(subtotals)
  → save Booking (+ cascaded BookingRooms)
  → 201 BookingResponse
```

---

## 7. Status machine

```
PENDING ──► CONFIRMED ──► CHECKED_IN ──► CHECKED_OUT
   │            │
   └──── CANCELLED ◄──────┘
```

Cannot cancel after check-in. Cannot skip steps (e.g. PENDING → CHECKED_IN).

**Soft-hold:** new bookings start as `PENDING` with `holdExpiresAt = now + app.booking.pending-hold-minutes` (default 15).  
`PendingBookingExpiryService` auto-cancels expired PENDING holds so unpaid carts do not block inventory forever.

---

## 8. Stretch checklist (Module 6)

| Task | Where |
|------|--------|
| Soft-hold PENDING expiry | `Booking.holdExpiresAt`, `PendingBookingExpiryService`, V8 |
| Block OUT_OF_SERVICE / non-AVAILABLE | `assertRoomOperationallyBookable` (AVAILABLE only) |
| Multi-room partial failure logging | `createBooking` logs `alreadyLockedRoomIds` then rethrows (TX rolls back) |
| Guest “my bookings” filter | `GET /bookings/guest/{id}?status=&from=&to=` |
| Overlap index review | V8: `idx_bookings_status_dates`, `idx_bookings_pending_hold`, guest+status+checkin |
| Sort whitelist + size cap 50 | `BookingPageables` |
| `excludeBookingId` | overlap queries + `GET /bookings/availability?excludeBookingId=` (future date-change API) |
| Concurrent create IT | `ConcurrentBookingCreateIntegrationTest` |
| Availability matrix (30d × rooms) | `GET /bookings/availability-matrix?from=&days=30` |

---

## 9. Try this

1. Availability: `GET /api/bookings/availability?checkInDate=2026-09-01&checkOutDate=2026-09-03`  
2. Matrix: `GET /api/bookings/availability-matrix?from=2026-09-01&days=30`  
3. Create booking with two room IDs  
4. Create overlapping booking → expect **409**  
5. My bookings: `GET /api/bookings/guest/1?status=PENDING`  
6. Run: `mvn test -Dtest=BookingRepositoryTest,BookingServiceTest,BookingControllerTest,ConcurrentBookingCreateIntegrationTest,PendingBookingExpiryServiceTest`

---

## 10. Interview warmup

1. Why is BookingRoom not a simple ManyToMany?  
2. Explain the overlap predicate.  
3. Why snapshot `pricePerNight` on BookingRoom?  
4. `@Transactional` — what rolls back if room 2 fails? Why still log partial failure?  
5. Pessimistic vs optimistic locking in this module?  
6. Why soft-hold PENDING instead of leaving carts forever?  
7. Why does an update-booking API need `excludeBookingId`?

---

## 11. What’s next

**Module 7 — Payment Management** records money against a booking (methods, status, paid amount) and ties confirmation to successful payment.

Apply MySQL stretch migration when needed:

```bash
mysql -h 127.0.0.1 -u root -p hotel_booking \
  < backend/src/main/resources/db/migration/V8__booking_module6_stretch.sql
```

Back to path: [MODULES.md](MODULES.md)
