# Module 14 — Testing (JUnit, Mockito, Integration & Frontend)

Enterprise-style automated testing for the Hotel Booking System. This module **adds tests and test utilities only** — production business logic, `pom.xml`, and `application.yml` stay unchanged.

---

## Why testing matters

Automated tests are a safety net: they catch regressions when you refactor booking overlap rules, JWT validation, or React route guards. In interviews and on teams, tests prove you can ship features without fear. They also document expected behaviour (“when a customer hits `/admin/**`, expect 403”).

---

## Concepts (interview-ready)

### 1. Unit vs integration

| Kind | Scope | Speed | Example here |
|------|-------|-------|--------------|
| **Unit** | One class, dependencies mocked | Fast | `JwtServiceTest`, `GuestServiceTest` |
| **Slice / repository** | JPA + H2 only | Medium | `@DataJpaTest` guest/room repos |
| **Integration** | Full Spring context + MockMvc + H2 | Slower | `AuthIntegrationTest`, `SecurityIntegrationTest` |
| **Frontend unit** | Component/hook with mocks | Fast | Vitest + RTL |
| **E2E** | Real browser | Slowest | Playwright `e2e/` |

### 2. Test pyramid

```
        /\
       /E2E\          few, critical journeys
      /------\
     / Integr.\       auth, booking, payment, security
    /----------\
   / Unit tests \     majority — services, JWT, utils, components
  /--------------\
```

Prefer many fast unit tests; keep a smaller set of integration tests on critical paths; reserve E2E for smoke journeys.

### 3. Test doubles (Mockito)

- **Mock** — fake with programmed behaviour (`when(...).thenReturn(...)`)
- **Stub** — mock used only for return values
- **Spy** — real object with some methods stubbed
- **Captor** — capture arguments (`ArgumentCaptor`) for assertions
- **Fake** — working lightweight impl (e.g. `MockRazorpayGateway`)

Annotations: `@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`.  
Spring: `@MockBean` replaces a bean in the context (use sparingly in `@SpringBootTest`).

### 4. MockMvc

Simulates HTTP without a real server. This project uses two styles:

1. **Standalone** (most Modules 4–9 controller tests): `MockMvcBuilders.standaloneSetup(controller)` + `GlobalExceptionHandler` — fast, no Security filter.
2. **`@AutoConfigureMockMvc` + `@SpringBootTest`**: real filter chain, JWT, RBAC — used in Module 14 security/integration suites.

### 5. Repository testing

`@DataJpaTest` + H2 (`application-test.yml`) loads only JPA slice. Assert custom JPQL, pagination, uniqueness.

### 6. Service testing

Pure Mockito: mock repositories/gateways, assert return values, thrown exceptions, and `verify(...)` interactions.

### 7. Controller testing

Assert status codes, JSON paths, validation errors. Prefer testing behaviour over implementation details.

### 8. React Testing Library

Query by role/label/text (how users see the UI). Avoid testing internal state. Prefer `userEvent` / `fireEvent` on accessible elements.

### 9. Coverage types

| Metric | Meaning |
|--------|---------|
| Statement | Lines executed |
| Branch | `if`/`switch` paths taken |
| Method | Methods entered |
| Class | Classes touched |

**Coverage is a signal, not a goal.** Prefer meaningful critical-path tests over chasing 100%.

### 10. Enterprise strategy

- Protect money/security paths first (auth, booking overlap, payments, admin RBAC).
- Shared fixtures (`TestDataFactory`) over copy-paste.
- Fast feedback in CI: unit → integration → optional E2E.
- Do not test framework code; test *your* rules.

---

## Testing flow

```
Arrange (builders / mocks)
        ↓
Act (call service / MockMvc / render)
        ↓
Assert (AssertJ / jsonPath / RTL)
        ↓
Coverage report (optional)
```

---

## Folder structure

### Backend (`backend/src/test/java/com/hotelbooking/`)

| Folder | Role |
|--------|------|
| `controller/` | Standalone MockMvc API tests |
| `service/` | Mockito unit tests |
| `repository/` | `@DataJpaTest` + H2 |
| `security/` | JWT, filter, RBAC integration |
| `integration/` | Multi-layer flows (auth, booking, payment, reports) |
| `exception/` | `GlobalExceptionHandler` mapping |
| `notification/` / `payment/` | Module 7–8 unit tests |
| `util/` | Factories, builders, MockMvc helpers, assertions |

### Frontend

| Path | Role |
|------|------|
| `src/test/` | Module 10–13 Vitest suites + `setup.js` |
| `src/tests/components/` | Module 14 component tests |
| `src/tests/pages/` | Page + route guard tests |
| `src/tests/hooks/` | Hook tests |
| `src/tests/services/` | API client tests |
| `src/tests/testUtils.jsx` | Shared RTL render helper |
| `e2e/` | Playwright journeys |

---

## How to run

### Backend (use JDK 21)

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo /Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home)
cd backend
mvn test
```

Focused:

```bash
mvn test -Dtest=SecurityIntegrationTest,AuthIntegrationTest,BookingFlowIntegrationTest,PaymentIntegrationTest
```

### Frontend

```bash
cd frontend
npm test
npm run test:coverage   # HTML + lcov under frontend/coverage/
```

### Backend coverage note

JaCoCo is **not** in `pom.xml`, and Module 14 must not modify `pom.xml`. Conceptually you would add the JaCoCo Maven plugin and open `target/site/jacoco/index.html`. Until then, rely on Surefire results + disciplined critical-path tests. Frontend coverage is configured via Vitest (`@vitest/coverage-v8`).

---

## What Module 14 added (highlights)

**Utilities:** `TestDataFactory`, `TestEntityBuilder`, `MockMvcTestSupport`, `IntegrationTestSupport`, `ErrorResponseAssert`

**Security:** `JwtServiceTest`, `JwtAuthenticationFilterTest`, `CustomUserDetailsServiceTest`, `SecurityIntegrationTest`

**Exception:** `GlobalExceptionHandlerTest`

**Integration:** `AuthIntegrationTest`, `BookingFlowIntegrationTest`, `PaymentIntegrationTest`, `ReportSecurityIntegrationTest`

**Other:** `PaymentReceiptServiceTest` (ArgumentCaptor), `MockRazorpayGatewayTest`, `ReportDateUtilsTest`, `UserRepositoryTest`

**Frontend:** Register, RoomCard, SearchBar, StatCard, ChartCard, OrderSummary, RoleRoute, BookingPaymentPage, authService, useDebouncedValue + coverage script

---

## Best practices

1. Name tests `method_shouldExpectedBehaviour_whenCondition`.
2. One logical behaviour per test.
3. Assert on public contracts (HTTP/JSON/UI), not private fields.
4. Clear SecurityContext / timers in `@AfterEach` when needed.
5. Prefer builders/factories for entity graphs.
6. Keep integration tests transactional (`@Transactional`) so H2 stays clean.
7. Never commit secrets; use `application-test.yml` test JWT secret only.

## Common beginner mistakes

- Using `git add .` / testing production bugs by changing prod code first.
- Over-mocking until the test only proves Mockito works.
- Ignoring Security (standalone MockMvc alone does not prove RBAC).
- Brittle RTL queries (`div.MuiBox-root` nth-child).
- Sleeping instead of `waitFor` / fake timers.
- Treating coverage % as quality.

---

## Practice assignment — completed solutions

### Exercises

| # | Task | Solution location | What it proves |
|---|------|-------------------|----------------|
| 1 | Blank email → 400 + field map | `GuestControllerTest#createGuest_blankEmail_returns400WithFieldMap` | Bean Validation runs in MockMvc; `GlobalExceptionHandler` fills `validationErrors.email` |
| 2 | Payment history by bookingId + status | `PaymentRepositoryTest#findByBookingId_andStatus_filtersPaymentHistory` | `@DataJpaTest` query filters for admin/customer payment history |
| 3 | Refund rejects PENDING | `PaymentServiceTest#refund_shouldRejectPendingPayment` | Domain rule: only SUCCESS payments refund; gateway never called |
| 4 | PrivateRoute → `/login` | `frontend/src/tests/pages/PrivateRoute.test.jsx` | Auth guard UX for unauthenticated users |
| 5 | PriceBreakdown tax line | `frontend/src/tests/components/PriceBreakdown.test.jsx` | Estimate UI shows GST row + tooltip |

### Mockito coding

| # | Task | Solution location | What it proves |
|---|------|-------------------|----------------|
| 1 | Capture saved `Guest` | `GuestServiceTest#createGuest_shouldPersistMappedGuest_capturedWithArgumentCaptor` | `ArgumentCaptor` asserts fields passed to `save` |
| 2 | Never email unknown user | `AuthServiceTest#forgotPassword_shouldNotRevealMissingUser` | `verify(..., never())` anti-enumeration |
| 3 | Stub overlap → exception | `BookingServiceTest#createBooking_shouldRejectOverlappingBooking` | Stub `existsOverlappingBooking(true)` → `RoomAlreadyBookedException` |

### E2E challenge

| Task | Solution | Notes |
|------|----------|--------|
| Login → search → book smoke | `frontend/e2e/login-search-book.spec.js` | API mocked (no Spring). Payment verify mocked. Run: `npm run test:e2e -- login-search-book` |

---


## 5-minute revision

- Pyramid: many unit, fewer integration, few E2E.
- Mockito: `@Mock` + `@InjectMocks` + `verify` + captors.
- MockMvc: standalone (fast) vs `@SpringBootTest` (security real).
- `@DataJpaTest` + H2 for queries.
- RTL: roles/labels, not implementation.
- Protect auth, booking, payment, admin RBAC first.
- Coverage informs gaps; critical paths matter more than %.

---

## Next: Module 15 — Production Deployment

Docker, CI/CD, AWS (or similar), monitoring, logging, and performance tuning. It is the final module because a correct app that cannot be deployed, observed, or scaled is not production-ready. Module 14’s suite becomes the gate you run in CI before every deploy.

---

*Module 14 complete — automated testing suite*
