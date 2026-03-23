# Learning by reverse engineering (after the app is “done”)

Yes — you can (and should) keep learning by reverse engineering even when the project is production-oriented.

Making the app safer does **not** remove the code. It adds constraints (secrets, ownership, fail-fast prod checks). The learning path is still:

**trace → break → explain → change a thin slice yourself.**

## Why production habits help learning

| Production change | What you learn by reverse-engineering it |
|-------------------|------------------------------------------|
| Ownership / IDOR guards | Why JWT subject must match guest email |
| Pessimistic lock + overlap | How concurrency is serialized in MySQL |
| Hold expiry in overlap query | Soft holds vs hard inventory |
| `ProductionSafetyRunner` | Fail-fast config vs silent misconfig |
| Bootstrap admin env vars | Why register must not grant ADMIN |
| Cancel blocked until refund | Money state machines |

## Reverse-engineering loop (45–90 min)

1. Pick one user action (e.g. “Pay for booking”).
2. Find the route in the frontend → API path.
3. Open Controller → Service → Repository → SQL/entity.
4. Draw a 5-box diagram on paper (no Cursor).
5. Break it (wrong email guest, expired hold, second parallel book).
6. Change ≤30 lines yourself, re-test that path only.

## Starter drills (order)

1. **Overlap race** — 2–3 parallel `POST /bookings` (you already did this).
2. **Ownership** — login as A, try `GET /bookings/guest/{B's guestId}` → expect 403.
3. **Hold expiry** — create PENDING, set `hold_expires_at` in the past in DB, try pay → rejected; try new book same room → allowed.
4. **Paid cancel** — SUCCESS payment then `PUT /bookings/{id}/cancel` → rejected until refund.
5. **Prod safety** — run with `SPRING_PROFILES_ACTIVE=prod` and weak `JWT_SECRET` → app refuses to start.

## Rule

Use Cursor to **explain after you have a hypothesis**, not to skip reading the stack.

Production-ready ≠ “nothing left to learn.” It means the system has sharper edges — better for deliberate practice.
