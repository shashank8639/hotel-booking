# Module 5 Guide — Room Management (Learn by Building)

This is your second full vertical slice. Guest Management taught the pattern. Room Management adds **inventory**, **pricing**, **availability**, and **images**.

---

## 1. What problem are we solving?

A hotel sells **rooms**, not only guest profiles.

Staff and customers need to:
- Browse rooms (type, price, capacity, photos)  
- Admins create/update/delete rooms  
- Admins change availability (AVAILABLE → RESERVED → OCCUPIED → CLEANING…)  
- Admins change base / discounted price (with currency)  
- Search with combined filters (including floor + description keywords)  
- See a day-by-day availability calendar  

That is **Room Management**.

---

## 2. Mental model

```
Public catalog                          Admin console
GET /rooms                              POST   /admin/rooms
GET /rooms/{id}                         PUT    /admin/rooms/{id}
GET /rooms/search                       PATCH  /admin/rooms/{id}/description
GET /rooms/{id}/images                  DELETE /admin/rooms/{id}   (soft-delete)
GET /rooms/{id}/availability-calendar   PUT    /admin/rooms/{id}/availability
GET /rooms/types                        PUT    /admin/rooms/{id}/pricing
GET /rooms/statuses                     POST   /admin/rooms/{id}/images
```

Security (already configured in Module 3):
- `GET /rooms/**` → public  
- `/admin/**` → **ADMIN only**

---

## 3. Guided tour (open in this order)

### A. Domain enums
- `database/RoomType.java` — STANDARD, DELUXE, EXECUTIVE, SUITE, FAMILY, PRESIDENTIAL  
- `database/RoomStatus.java` — AVAILABLE, RESERVED, OCCUPIED, **CLEANING**, MAINTENANCE, OUT_OF_SERVICE  

**Mentor tip:** Room status ≠ Booking status. Room status is inventory state; booking status is reservation lifecycle.

### B. Entities
- `entity/Room.java` — pricing, `currency`, `deleted` soft-flag, discount CHECK, images, seasonal prices  
- `entity/RoomImage.java` — **URL/metadata only** (not binary files; ready for S3 later)  
- `entity/SeasonalRoomPrice.java` — date-range rates (peak / off-season design)

### C. Repository
- `RoomRepository` — uniqueness, combined `searchRooms` (floor + description keywords), soft-delete aware  
- `SeasonalRoomPriceRepository` — cover a night for a room  
- `RoomImageRepository` — images by room  

### D. Service brain
Open `service/impl/RoomServiceImpl.java` and find:
1. Duplicate room number check (active rooms only)  
2. Discount ≤ base price rule (+ DB CHECK)  
3. Allowed status transitions map (includes CLEANING)  
4. Soft-delete (`deleted = true`) instead of hard delete  
5. `RoomPageables` — max page size **50**, sort field whitelist  
6. Availability calendar — per-night BOOKED / operational block  

### E. Controllers
- `RoomController` — thin public reads + calendar  
- `AdminRoomController` — mutations under `/admin/rooms` (incl. PATCH description)  

### F. Migrations
```bash
mysql -h 127.0.0.1 -u root -p hotel_booking \
  < backend/src/main/resources/db/migration/V5__room_management_enhancements.sql
mysql -h 127.0.0.1 -u root -p hotel_booking \
  < backend/src/main/resources/db/migration/V7__room_module5_stretch.sql
```

---

## 4. Follow one admin create request

```
POST /api/admin/rooms + JWT (ADMIN)
  → SecurityConfig checks ROLE_ADMIN
  → AdminRoomController (@Valid)
  → RoomServiceImpl (duplicate + pricing rules)
  → RoomMapper → Room entity
  → RoomRepository.save → INSERT rooms
  → 201 + RoomResponse
```

---

## 5. Availability transitions (business rules)

```
AVAILABLE → RESERVED → OCCUPIED → CLEANING → AVAILABLE
    |          |           |           |
    v          v           v           v
MAINTENANCE / OUT_OF_SERVICE (limited paths back)
```

Invalid example: `AVAILABLE → OCCUPIED` directly → `400`.  
After checkout, rooms go **OCCUPIED → CLEANING → AVAILABLE** (housekeeping).

---

## 6. Stretch checklist (this module)

| Task | Where |
|------|--------|
| PATCH description only | `PATCH /admin/rooms/{id}/description` |
| Soft-delete flag | `Room.deleted`; `DELETE` soft-hides |
| Floor filter + description search | `GET /rooms/search?floorNumber=&description=` |
| Page size cap 50 | `RoomPageables.MAX_PAGE_SIZE` |
| Sort whitelist | `RoomPageables.ALLOWED_SORT_FIELDS` |
| Currency + seasonal table | `Room.currency`, `seasonal_room_prices` / `SeasonalRoomPrice` |
| CLEANING status | `RoomStatus.CLEANING` + transitions |
| Discount CHECK `@DataJpaTest` | `RoomRepositoryTest.discountedPriceAboveBase_shouldViolateCheckConstraint` |
| Availability calendar | `GET /rooms/{id}/availability-calendar?from=&to=` |

**Seasonal pricing design:** for night `d`, prefer a covering `SeasonalRoomPrice`, else `discountedPrice`, else `pricePerNight`. Overlapping seasons for one room should be avoided at write time.

**Description search:** JPQL keyword `LIKE` (portable for H2 tests); MySQL `FULLTEXT` index `ft_rooms_description` in V7 for production scale.

---

## 7. Try this

1. List types: `GET /api/rooms/types`  
2. Search: `GET /api/rooms/search?roomType=DELUXE&floorNumber=2&description=view&size=50`  
3. Calendar: `GET /api/rooms/1/availability-calendar?from=2026-09-01&to=2026-09-07`  
4. Run tests: `mvn test -Dtest=RoomRepositoryTest,RoomServiceTest,RoomControllerTest,AdminRoomControllerTest`  

---

## 8. Interview warmup

1. Why soft-delete rooms instead of hard delete when bookings exist?  
2. Why whitelist sort fields?  
3. Room status vs booking status? Why CLEANING?  
4. Why store image URLs instead of bytes?  
5. How would you resolve seasonal vs discounted vs base price for a 3-night stay?  

---

## 9. What’s next

**Module 6 — Booking Management** connects Guest + Room into a reservation with date overlap rules and payments.

Back to path: [MODULES.md](MODULES.md)
