# Module 11 Guide — Public Website (Single-Property Hotel)

Module 10 gave you login. Module 11 gives guests something worth logging in for: a **public marketing + room search + book** experience on top of existing Spring APIs.

> This backend is **single-hotel room inventory** (no `Hotel` entity). The UI still uses familiar OTA patterns (hero, search, cards, details) but cards represent **rooms** at Grand Horizon Hotel.

---

## 1. How hotel booking websites are designed

Typical layers:

1. **Acquire** — landing hero, trust, destinations, CTA  
2. **Discover** — search, filters, sort, pagination  
3. **Decide** — details, gallery, amenities, policies  
4. **Convert** — book (auth gate) → summary → payment  

Enterprise tip: marketing content can be CMS-driven later; inventory must stay API-driven.

---

## 2–4. Component architecture & MUI layout

| Folder | Role |
|--------|------|
| `pages/` | Route-level screens |
| `components/home` | Landing building blocks |
| `components/rooms` | Cards, filters, gallery |
| `components/common` | Footer, skeletons, errors |
| `hooks/` | Data fetching (`useRoomSearch`) |
| `services/` | Axios API wrappers |
| `assets/` | Brand copy & fallback images |

MUI: `Container` for readable width, `Grid` for responsive columns, `sx` breakpoints (`xs` → `md`).

---

## 5–8. Routing, search, pagination

- Search state lives in the **URL** (`/rooms?checkIn=…&roomType=DELUXE`) — shareable and refresh-safe.  
- Results call `GET /api/rooms/search` with `page` / `size` / `sort`.  
- Spring `Page` → MUI `Pagination`.

---

## 9–12. Performance ideas (what we started / what’s next)

- Skeletons while loading  
- Fallback images when no uploads  
- Later: `React.lazy` route splitting, image `loading="lazy"`, virtualized long lists  

---

## Booking flow (reuses Modules 6–7 + 10)

```
Book Now → login if needed → /book
  → find/create Guest by user email
  → POST /bookings
  → /checkout/:id → POST /payments/create-order
```

User ≠ Guest: login account must resolve to a `guestId` for the Booking API.

---

## Routes added

| Path | Auth | Purpose |
|------|------|---------|
| `/` | Public | Landing |
| `/rooms` | Public | Search results |
| `/rooms/:id` | Public | Details |
| `/book` | Private | Booking form |
| `/checkout/:bookingId` | Private | Payment order summary |
| `*` | Public | 404 |

---

## Run

```bash
cd backend && mvn spring-boot:run
cd frontend && npm run dev
```

```bash
cd frontend && npm test && npm run build
```

---

*Last updated: Module 11 — Public Website*
