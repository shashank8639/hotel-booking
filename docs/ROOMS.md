# Module 5 Guide — Room Management (Learn by Building)

This is your second full vertical slice. Guest Management taught the pattern. Room Management adds **inventory**, **pricing**, **availability**, and **images**.

---

## 1. What problem are we solving?

A hotel sells **rooms**, not only guest profiles.

Staff and customers need to:
- Browse rooms (type, price, capacity, photos)  
- Admins create/update/delete rooms  
- Admins change availability (AVAILABLE → RESERVED → OCCUPIED…)  
- Admins change base / discounted price  
- Search with combined filters  

That is **Room Management**.

---

## 2. Mental model

```
Public catalog                          Admin console
GET /rooms                              POST   /admin/rooms
GET /rooms/{id}                         PUT    /admin/rooms/{id}
GET /rooms/search                       DELETE /admin/rooms/{id}
GET /rooms/{id}/images                  PUT    /admin/rooms/{id}/availability
GET /rooms/types                        PUT    /admin/rooms/{id}/pricing
                                        POST   /admin/rooms/{id}/images
```

Security (already configured in Module 3):
- `GET /rooms/**` → public  
- `/admin/**` → **ADMIN only**

---

## 3. Guided tour (open in this order)

### A. Domain enums
- `database/RoomType.java` — STANDARD, DELUXE, EXECUTIVE, SUITE, FAMILY, PRESIDENTIAL  
- `database/RoomStatus.java` — AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE, OUT_OF_SERVICE  

**Mentor tip:** Room status ≠ Booking status. Room status is inventory state; booking status is reservation lifecycle.

### B. Entities
- `entity/Room.java` — pricing (`pricePerNight`, `discountedPrice`), status, images  
- `entity/RoomImage.java` — **URL/metadata only** (not binary files; ready for S3 later)

### C. Repository
- `RoomRepository` — uniqueness, price range, combined `searchRooms(...)` query  
- `RoomImageRepository` — images by room  

### D. Service brain
Open `service/impl/RoomServiceImpl.java` and find:
1. Duplicate room number check  
2. Discount ≤ base price rule  
3. Allowed status transitions map  
4. Delete blocked when booking history exists  

### E. Controllers
- `RoomController` — thin public reads  
- `AdminRoomController` — mutations under `/admin/rooms`  

### F. Migration
Apply when using MySQL:

```bash
mysql -h 127.0.0.1 -u root -p hotel_booking \
  < backend/src/main/resources/db/migration/V5__room_management_enhancements.sql
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
AVAILABLE → RESERVED → OCCUPIED → AVAILABLE
    |          |           |
    v          v           v
MAINTENANCE / OUT_OF_SERVICE (limited paths back)
```

Invalid example: `AVAILABLE → OCCUPIED` directly → `400`.

---

## 6. Try this

1. List types: `GET /api/rooms/types`  
2. Search: `GET /api/rooms/search?roomType=DELUXE&status=AVAILABLE&minPrice=1000&maxPrice=5000`  
3. Run tests: `mvn test -Dtest=RoomRepositoryTest,RoomServiceTest,RoomControllerTest,AdminRoomControllerTest`  
4. Explain why images are URLs, not BLOBs in MySQL  

---

## 7. Interview warmup

1. Why separate `/rooms` (public) from `/admin/rooms`?  
2. Why `BigDecimal` for price?  
3. Room status vs booking status?  
4. Why store image URLs instead of bytes?  
5. Why block delete when `booking_rooms` exist?  

---

## 8. What’s next

**Module 6 — Booking Management** connects Guest + Room into a reservation with date overlap rules and payments.

Back to path: [MODULES.md](MODULES.md)
