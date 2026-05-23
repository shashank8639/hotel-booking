# Module 13 — React Admin Dashboard

Admin operations portal on existing Spring Boot APIs (Modules 4–9).  
**No backend API changes.** Charts use **Recharts** (added as a new dependency; other packages not upgraded).

---

## Teach-first concepts

### 1. Enterprise dashboard architecture

Separate **public site chrome** from **admin chrome**:

- Public: marketing navbar + footer  
- Admin: persistent sidebar + dense data tables + KPIs  

Admins authenticate (JWT + `ROLE_ADMIN`), then hit read models (`/admin/dashboard`, reports) and CRUD APIs (`/admin/rooms`, `/guests`, `/bookings`, `/payments`).

### 2. Material UI dashboard design

- `Drawer` + `AppBar` + `Toolbar`  
- Dense `Table` + `TablePagination`  
- `Dialog` for CRUD / confirm  
- `Snackbar` for toast feedback  
- Cards for KPIs, not for every row  

### 3. Responsive admin layouts

- `md+`: permanent drawer  
- `< md`: temporary drawer + menu icon  
- Stack filters column → row  

### 4. Dashboard routing

```
/admin → AdminLayout
  /admin/dashboard
  /admin/rooms
  /admin/guests
  /admin/bookings
  /admin/payments
  /admin/reports
```

Guarded by `PrivateRoute` + `RoleRoute([ADMIN])`. Modules lazy-loaded.

### 5. Sidebar navigation

`ADMIN_NAV` config drives `ListItemButton` links — single source for labels/icons/paths.

### 6. Chart libraries in React

**Recharts** chosen for declarative React components (`LineChart`, `BarChart`, `PieChart`). Data adapted from `LabeledAmountDto` via `toChartRows`.

### 7. Data visualization best practices

- One question per chart  
- Label axes / use tooltips  
- Prefer server aggregates (Module 9) over charting raw row dumps  
- Show loading skeletons  

### 8. Dashboard performance

- Parallel `Promise.all` for KPI + charts  
- Lazy route chunks for admin pages  
- Paginated tables (never load all bookings)  

### 9. Pagination strategies

Spring `Page` → `content`, `totalElements`, `number`, `size`.  
`usePagedResource` centralizes page/size/filter state.

### 10. Search and filtering

- Rooms: query params on `/rooms/search`  
- Guests: name/email/phone search endpoints  
- Bookings: `/bookings/status/{status}`  
- Payments: `/payments/history` filters  

### 11. Lazy loading dashboard modules

`React.lazy` + `Suspense` for each admin page — smaller initial bundle for public visitors.

### 12. Enterprise React admin architecture

```
Pages → Components → Hooks → Services → Axios → Spring Boot → MySQL
```

`AdminUiContext` = cross-cutting toasts only (not server cache).

---

## Folder map

| Folder | Role |
|--------|------|
| `layouts/AdminLayout.jsx` | Sidebar + top bar shell |
| `pages/admin/` | Route screens |
| `components/admin/` | Tables helpers, dialogs, KPIs |
| `charts/` | Recharts wrappers |
| `services/admin*.js` | Report + admin room clients |
| `hooks/useAdminDashboard.js` | Dashboard data load |
| `hooks/usePagedResource.js` | Generic Spring Page loader |
| `context/AdminUiContext.jsx` | Snackbars |
| `utils/adminDates.js` | Date ranges + chart mapping |

---

## APIs reused

- `GET /admin/dashboard`  
- `GET /admin/reports/**`  
- `GET /rooms/search`, `GET /rooms/{id}/images`  
- `POST|PUT|DELETE /admin/rooms/**`  
- `GET|POST|PUT|DELETE /guests/**`  
- `GET|PUT /bookings/**`  
- `GET /payments/history`, refund, invoice PDF  

---

## Run

```bash
cd frontend
npm test
npm run build
```

Login as ADMIN → `/admin/dashboard`.

---

## Practice solutions (implemented)

| Exercise | Solution |
|----------|----------|
| (1) Refresh on dashboard | `dashboard-refresh` button → `useAdminDashboard().reload()` |
| (2) Debounce room search | Local input + `useDebouncedValue(…, 400)` → `patchFilters` |
| (3) Empty-state illustration | `AdminEmptyState` inside `AdminDataTable` when `rows.length === 0` |
| (4) Highlight CANCELLED | `getRowSx` + error-tint Chip on bookings table |
| (5) Persist report dates | `reportRangeStorage.js` ↔ `sessionStorage` in `AdminReportsPage` |

| Coding | Solution |
|--------|----------|
| Shared `AdminDataTable` | `components/admin/AdminDataTable.jsx` (+ sort helpers) |
| MSW `/admin/dashboard` | `mocks/handlers.js`, `mocks/server.js`, covered in `adminPractice.test.jsx` |
| Room column sort | `TableSortLabel` → Spring `sort=field,asc\|desc` |

| Challenge | Solution |
|-----------|----------|
| 60s check-ins poll | `useTodaysCheckInsPoll` — `AbortController` + `setInterval(60_000)` → KPI card |

```bash
cd frontend && npm test
```
