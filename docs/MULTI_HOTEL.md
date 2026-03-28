# Module 16 — Multi-Hotel Platform (Telangana Expansion)

> **Product shift:** Modules 1–15 = one branded hotel (Grand Horizon).  
> Module 16 = **marketplace / OTA-style** platform: many hotels across Telangana, scalable to India → world.

---

## Part A — Teach first (why this architecture)

### 1. Why Booking.com uses a Hotel entity

Guests do not book “a room in the abstract.” They book a **property**: brand, location, star rating, photos, policies, cancellation rules, and reputation.

Without `Hotel`:
- You cannot search “hotels in Hyderabad”
- You cannot show property-level reviews/amenities
- Room numbers collide across properties (`101` exists everywhere)

`Hotel` is the **catalog unit**. `Room` is the **inventory unit**.

### 2. Why Room must belong to Hotel

A room is physical inventory owned by one property:

```text
Hotel (Hyderabad Grand) ──1──*── Room (101, 102, Suite…)
```

Business rules that depend on this:
- Availability is per hotel’s rooms
- Pricing / seasonal rates are per hotel (or room under hotel)
- Owner dashboard shows “my hotels → my rooms”
- Booking still references rooms (Modules 6–7 stay valid)

**Invariant:** every room has exactly one `hotel_id`.

### 3. Why location hierarchy matters

Users search by **place**, not by hotel id:

```text
“Hyderabad this weekend” → City → Hotels → Rooms → Booking
```

Hierarchy enables:
- Faceted filters (city, then price, stars)
- Popular destinations (aggregate by city)
- SEO pages (`/hotels/hyderabad`)
- Later: geofence / “near me” using city/geo coordinates

### 4. Why Country → State → City → Hotel scales

```text
Country (India)
  └── State (Telangana)
        └── City (Hyderabad, Warangal, …)
              └── Hotel
```

| Scope today | Tomorrow without redesign |
|-------------|---------------------------|
| Telangana cities | Add more `states` rows |
| India | Add more states under India |
| Worldwide | Add countries + states + cities |

You **never** hardcode “Telangana-only” into Java enums. Geography is **data**.

### 5. How enterprise OTAs work (simplified)

```text
Guest → Search Service (filters + availability)
      → Hotel Catalog (content, images, amenities)
      → Inventory / Availability (rooms free for date range)
      → Booking → Payment → Confirmation (+ email)
```

Separate **concerns**:
- **Catalog** (what hotels exist, content)
- **Inventory** (what can be sold for dates)
- **Commerce** (booking + payment) — you already built this

### 6. Database normalization (hotel platforms)

| Table | Responsibility |
|-------|----------------|
| `countries` / `states` / `cities` | Geo reference data |
| `hotels` | Property master |
| `hotel_images` | 1:N media |
| `amenities` + `hotel_amenities` | M:N reusable facilities |
| `hotel_reviews` | Guest feedback + rating rollups |
| `rooms.hotel_id` | Inventory ownership |
| `bookings` / `booking_rooms` | Commerce (unchanged flow) |

Avoid stuffing amenities as CSV on `hotels` — that breaks filtering.

### 7. Search architecture

**MVP (this module):** SQL / JPA filters + indexes on `city_id`, `status`, `star_rating`, `min_price`, `featured`.

**Later (Module 17+):** Elasticsearch / OpenSearch for full-text + geo; Redis for hot destination cards.

Public search returns **only `APPROVED` + verified** hotels.

### 8. Availability management

Do **not** invent a second source of truth yet. Reuse Module 5–6 overlap logic:

```text
Room is available for [checkIn, checkOut)
  iff no CONFIRMED/PENDING booking overlaps those dates
  and room.status allows sale
```

Optional `room_availability` calendar tables appear at scale for denormalized speed — not required for Telangana MVP.

### 9. Hotel ownership model

| Role | Sees |
|------|------|
| `CUSTOMER` | Search, book, pay, own bookings |
| `HOTEL_OWNER` | Register hotel, rooms, prices, images, own bookings/revenue |
| `ADMIN` | Approve/reject hotels, cities, amenities, platform ops |

`hotel_owners` (or `hotels.owner_user_id`) links `users` → hotels.

### 10. Why this scales globally

- Geo is relational reference data  
- Hotel is multi-tenant by `hotel_id` on inventory  
- Booking/payment modules stay hotel-agnostic (they book **rooms**)  
- Search can swap SQL → search engine without changing booking APIs  
- New markets = new rows, not new codebases  

---

## Part B — What we implement in this repo (MVP → stretch)

### Phase 1 (foundation — shipped in Module 16 code)

- Geo tables + Telangana seed cities  
- `hotels` + images + amenities + reviews + policies  
- `rooms.hotel_id` backfilled to **Grand Horizon, Hyderabad** (compat)  
- Public hotel search APIs  
- Landing → hotel list → hotel detail → rooms → existing book/pay  
- Admin hotel approval endpoints  
- Owner role + basic owner hotel CRUD  

### Phase 2 (follow-ups / practice)

- Full owner revenue dashboard UI  
- Wishlist / recently viewed  
- Map tiles + “near me” (lat/lng)  
- Recommendation engine (Module 17 preview)

---

## Backward compatibility

| Old behaviour | After Module 16 |
|---------------|-----------------|
| Browse `/rooms` | Still works; rooms belong to Grand Horizon |
| Book → pay → email | Unchanged |
| Grand Horizon branding | One hotel among many; platform brand on landing |

---

## Request flow (target)

```text
Guest → Search Hyderabad → Hotel results → Hotel details
     → Available rooms → Booking → Payment → Confirmation
```

## Architecture

```text
React → Spring Boot
         ├─ HotelService (catalog + search + approval)
         ├─ RoomService (inventory; now hotel-scoped)
         ├─ BookingService / PaymentService (commerce)
         └─ MySQL
```

---

## Module 17 preview (do not build yet)

Nationwide India: all states, geolocation/maps, advanced search (OpenSearch), recommendations, multi-region cloud deploy.

---

## API list (Module 16)

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | `/cities` | Public | Telangana cities |
| GET | `/cities/popular` | Public | Destination cards |
| GET | `/hotels/search` | Public | Filtered hotel catalog |
| GET | `/hotels/featured` | Public | Featured strip |
| GET | `/hotels/{slug}` | Public | Hotel detail |
| GET | `/hotels/{slug}/rooms` | Public | Rooms for hotel |
| GET | `/admin/hotels/pending` | ADMIN | Approval queue |
| POST | `/admin/hotels/{id}/approve` | ADMIN | Approve listing |
| POST | `/admin/hotels/{id}/reject` | ADMIN | Reject listing |
| GET | `/owner/hotels` | HOTEL_OWNER | My properties |
| POST | `/owner/hotels` | HOTEL_OWNER | Submit hotel |

---

## ER (simplified)

```text
countries 1─* states 1─* cities 1─* hotels
hotels 1─* rooms 1─* booking_rooms *─1 bookings
hotels 1─* hotel_images
hotels *─* amenities
hotels 1─* hotel_reviews
hotels 1─* hotel_policies
users 1─* hotels (owner)
```

---

## 20 interview questions (sample answers in study)

1. Why separate Hotel from Room?  
2. Why geo hierarchy instead of a free-text city string?  
3. How do you keep Modules 1–15 bookings working after multi-hotel?  
4. What indexes help hotel search?  
5. Why only APPROVED+verified hotels in public search?  
6. How would you add Elasticsearch without rewriting booking?  
7. Ownership model: role vs `owner_user_id`?  
8. How do amenity M:N filters work efficiently?  
9. Soft vs hard delete for hotels?  
10. How does availability still use booking overlap?  
11. Unique `(hotel_id, room_number)` vs global room number?  
12. How do you paginate search with amenity HAVING filters?  
13. CDN for hotel images?  
14. Multi-tenant security risks (owner A editing hotel B)?  
15. Denormalized `min_price` / `avg_rating` trade-offs?  
16. How to expand Telangana → India without schema change?  
17. Idempotent hotel slug generation?  
18. Why Flyway/manual SQL over Hibernate `ddl-auto` in prod?  
19. How would Airbnb “listing” map to Hotel+Room here?  
20. What breaks if `open-in-view=false` and you JOIN FETCH poorly?

---

## Practice assignment

1. Apply `V12__multi_hotel_platform.sql`.  
2. Restart backend; open `/hotels?citySlug=hyderabad`.  
3. Open Grand Horizon detail → book a room → pay (mock).  
4. Register a user, grant `HOTEL_OWNER`, POST a new hotel, approve as ADMIN.  
5. Add amenity filter `amenities=WIFI,POOL` on search UI.

## Build verification

```bash
# DB
mysql ... < backend/src/main/resources/db/migration/V12__multi_hotel_platform.sql
# Backend
cd backend && set -a && source .env && set +a && mvn spring-boot:run
# Frontend
cd frontend && npm run dev
```

## Common beginner mistakes

- Searching rooms before choosing a hotel (OTA UX expects hotel first)  
- Forgetting to backfill `rooms.hotel_id`  
- Putting amenities as CSV on hotels  
- Hardcoding city names in Java enums  
- Exposing PENDING hotels publicly  

## 10-minute revision

Hotel = catalog · Room = inventory · Geo = data · Search = filters + indexes · Approval = trust · Booking/payment modules stay room-centric · Scale by adding rows not rewriting commerce.
