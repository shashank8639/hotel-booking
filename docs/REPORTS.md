# Module 9 Guide — Reports & Admin Dashboard (Learn by Building)

Modules 4–8 write operational data (guests, rooms, bookings, payments, emails).  
Module 9 **reads that data and turns it into decisions** for hotel managers.

---

## Constraint note

`pom.xml` and `application.yml` stay frozen. Reporting uses existing entities + JPQL via `EntityManager` — no new analytics DB, no Redis cache dependency yet.

---

## 1. What reporting systems are

A **reporting system** answers management questions from transactional data:

| Question | Report type |
|----------|-------------|
| How much money did we collect? | Revenue |
| How full is the hotel? | Occupancy |
| Which bookings cancelled? | Booking analytics |
| How did August look overall? | Monthly consolidated |

In enterprises, reporting often sits beside OLTP (day-to-day CRUD) as **read models**, warehouses, or BI tools. Here we implement **API-first analytics** inside the same Spring Boot app — a common first step before a data warehouse.

---

## 2. How dashboards work

A dashboard is a **single screen of KPIs** (key performance indicators), not a dump of every row.

```
Admin UI  →  GET /admin/dashboard  →  ReportService  →  many COUNT/SUM queries  →  one JSON summary
```

Managers want “today’s check-ins” and “this month’s revenue” in under a second. That is why we aggregate in SQL/`EntityManager`, not load all bookings into Java and loop.

---

## 3. Aggregation queries (SUM / COUNT / GROUP BY)

```sql
SELECT status, SUM(amount), COUNT(*)
FROM payments
WHERE paid_at >= ? AND paid_at < ?
GROUP BY status
```

- **SUM** → money totals  
- **COUNT** → volume  
- **GROUP BY** → buckets for charts (`byStatus`, `byRoomType`, daily series)  
- **HAVING** → filter groups after aggregation (e.g. guests with COUNT > 5) — useful later  

`ReportQueryRepository` is dedicated to these read models so CRUD repositories stay thin.

---

## 4. OLTP vs reporting

| | OLTP (Modules 4–8) | Reporting (Module 9) |
|--|--------------------|----------------------|
| Goal | Correct single transactions | Trends & totals |
| Writes | Frequent | Rare / none |
| Reads | By primary key / small lists | Scans + aggregates |
| Indexes | Uniqueness, FK lookups | Date columns, status + date |
| Latency target | Milliseconds per write | Sub-second for dashboard |

Same MySQL tables; different query shapes.

---

## 5. Why GROUP BY matters

Without `GROUP BY`, you get one total. Charts need **series**: revenue per day, bookings per status.  
Interview tip: `SELECT` non-aggregated columns must appear in `GROUP BY` (SQL rule).

---

## 6. Occupancy percentage

**Snapshot (right now):**

```
currentOccupancy% = occupiedRooms / totalRooms × 100
```

**Period (over dates):**

```
capacityRoomNights = totalRooms × nightsInRange
bookedRoomNights   = sum of overlapping non-cancelled booking-room nights (clipped to range)
periodOccupancy%   = bookedRoomNights / capacityRoomNights × 100
```

`endDate` on the API is **inclusive**. Internally we use half-open `[start, endExclusive)`.

---

## 7. Revenue reports

- Count **SUCCESS** payments by `paidAt` in range  
- Subtract refunds (`refundedAmount`) for **net revenue**  
- Series: daily (`FUNCTION('DATE', paidAt)`) or monthly (`YEAR`/`MONTH`)  
- Breakdowns: room type, booking status, payment status  

---

## 8. Date-range filtering

Always prefer **half-open intervals** in queries:

```
paidAt >= startOfDay(start) AND paidAt < startOfDay(end + 1 day)
```

Avoids “end of day” timezone bugs. Validation: start ≤ end (`ReportDateUtils` / `InvalidReportFilterException` → HTTP 400).

---

## 9. Dashboard caching strategies (enterprise next step)

| Strategy | When |
|----------|------|
| Short TTL cache (Redis, 30–60s) | Hot dashboard endpoint |
| Materialized daily rollup table | Heavy historical ranges |
| Read replica | Isolate reporting load from booking writes |
| Pre-compute nightly jobs | Monthly PDFs / emails |

We do not add Redis yet (pom frozen). Know the options for interviews.

---

## 10. Performance tips used here

1. Aggregate in the database, not in Java loops (except occupancy clip).  
2. Separate `ReportQueryRepository` from CRUD.  
3. Index recommendations: `payments(status, paid_at)`, `bookings(created_at)`, `bookings(check_in_date)`, `rooms(status)`.  
4. Cap “top N” guest/room charts with `setMaxResults`.  
5. `JOIN FETCH` recent bookings to avoid N+1 on guest name.

---

## 11. Enterprise reporting architecture (where this fits)

```
OLTP App (this module)  ──optional──►  Event stream / CDC  ──►  Warehouse / BI
         │
         └── REST analytics for admin UI (what we built)
```

Start with API aggregates; grow to warehouse when reports become heavy or cross-system.

---

## Part F practice checklist

| Task | Status | Where |
|------|--------|--------|
| Average booking value | Done | `BookingReportResponse.averageBookingValue` |
| Weekly revenue buckets (same path) | Done | `GET /admin/reports/revenue?period=WEEKLY` → ISO week labels |
| Monthly CSV export (bytes) | Done | `GET /admin/reports/monthly/export` → `text/csv` bytes |
| Top guests `HAVING COUNT(b) > 3` | Done | `ReportQueryRepository.bookingsByGuest` |
| Index DDL (documented, not applied) | Done | `db/migration/V11__report_indexes.sql.example` |
| Occupancy `totalRooms = 0` unit test | Done | `ReportServiceTest` |
| Payment report `FAILED` only | Done | `status=FAILED` filters totals + daily series |
| Occupancy range cap 90 days → 400 | Done | `ReportDateUtils.validateMaxInclusiveDays` |

---

## APIs (ADMIN JWT required)

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/admin/dashboard` | KPI summary |
| GET | `/admin/reports/revenue` | Revenue + period series (`DAILY`/`WEEKLY`/`MONTHLY`) |
| GET | `/admin/reports/revenue/date-range` | Revenue (daily series) |
| GET | `/admin/reports/occupancy` | Occupancy & utilization (max **90** inclusive days) |
| GET | `/admin/reports/bookings` | Booking analytics (+ average booking value) |
| GET | `/admin/reports/bookings/status` | Filter by booking status |
| GET | `/admin/reports/monthly` | Month rollup (JSON) |
| GET | `/admin/reports/monthly/export` | Month rollup (**CSV bytes**) |
| GET | `/admin/reports/payments` | Payment analytics (`status=FAILED` supported) |

Swagger tag: **Admin Reports**. Security: `/admin/**` → `ROLE_ADMIN`.

---

## Files to open

| File | Why |
|------|-----|
| `controller/AdminReportController.java` | Thin HTTP + OpenAPI + CSV export |
| `service/ReportService.java` + `impl/ReportServiceImpl.java` | KPI math, weekly buckets, occupancy cap |
| `repository/ReportQueryRepository.java` | SUM/COUNT/GROUP BY/`HAVING` JPQL |
| `dto/report/*` | Chart-friendly response shapes |
| `util/ReportDateUtils.java` | Inclusive API dates → exclusive bounds + 90-day cap |
| `exception/InvalidReportFilterException.java` | 400 on bad filters |
| `V11__report_indexes.sql.example` | Suggested indexes (Flyway-ignored) |

---

## Try this

1. Call `/admin/dashboard` with an ADMIN token after seeding payments.  
2. Change a SUCCESS payment’s `paidAt` and re-run revenue for that day.  
3. `GET /admin/reports/revenue?period=WEEKLY` — series labels like `2026-W32`.  
4. `GET /admin/reports/monthly/export?year=2026&month=8` — download CSV.  
5. Occupancy spanning >90 days → **400**.  
6. Explain why cancelled bookings are excluded from occupancy room-nights.

---

## Tests

- `ReportServiceTest` — KPIs, net revenue, occupancy %, zero rooms, 90-day cap, weekly buckets, FAILED filter, CSV  
- `AdminReportControllerTest` — HTTP mapping + CSV bytes + 400 mapping  
- `ReportQueryRepositoryTest` — real JPQL against test DB  

`mvn test -Dtest=ReportServiceTest,AdminReportControllerTest`

---

## What’s next

**Module 10 — React Authentication** ([REACT_AUTH.md](REACT_AUTH.md)) — SPA login that can later call these admin report APIs with an ADMIN JWT.

Path index: [MODULES.md](MODULES.md)

---

*Last updated: Module 9 Part F practice complete*
