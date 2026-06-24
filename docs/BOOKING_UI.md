# Module 12 — Booking UI

Multi-step booking wizard on top of existing Spring Boot booking & payment APIs.  
**No backend API changes.** Auth, guests, bookings, payments, invoices are reused.

---

## Teach-first: concepts interviewers expect

### 1. Multi-step booking workflow

Enterprise checkout is a **wizard** (stepper), not one giant form:

1. **Select Room / stay** — dates, party size, availability probe  
2. **Guest Details** — booking party (Guest ≠ User)  
3. **Booking Summary** — price breakdown + confirm → `POST /bookings`  
4. **Payment** — `POST /payments/create-order` → verify  
5. **Confirmation** — booking id, invoice, email message  

Why steps? Smaller validation surface, clearer recovery after failure, better analytics (“drop-off at payment”).

### 2. Controlled vs uncontrolled forms

| Style | Who owns value | Typical use |
|-------|----------------|-------------|
| **Controlled** | React state / RHF | Almost all MUI booking forms |
| **Uncontrolled** | DOM (`ref` / defaultValue) | Rare; file inputs, tiny demos |

MUI `TextField` is easiest as **controlled** (`value` + `onChange`). React Hook Form’s `Controller` bridges MUI ↔ RHF.

### 3. React Hook Form

We use `react-hook-form` for booking + guest steps:

- `useForm` — register defaults, errors, submit  
- `Controller` — wrap MUI fields  
- `mode: 'onBlur'` — validate when user leaves a field  

Benefits: fewer re-renders than “every keystroke updates parent state”, built-in error maps, easy `handleSubmit`.

### 4. Form validation

Two layers (enterprise habit):

1. **UI rules** — RHF `rules` + `utils/bookingValidation.js` (dates, email, phone, capacity)  
2. **API rules** — Spring `@Valid` on `GuestRequest` / `BookingRequest` (never trust the client)

### 5. Booking summary calculation

`utils/priceCalculation.js` computes **display** estimates:

- Room charges = rate × nights  
- Discount = list rate − effective rate  
- Taxes / service = % of room charges  
- Grand total = room + tax + service  

**Authoritative total** is still `BookingResponse.totalAmount` from the Booking Engine after create.

### 6. Integrating React with payment APIs

```
create booking → create-order → (Checkout / mock sign) → verify → success|failure
```

- `paymentService.createOrder(bookingId)`  
- Mock path: HMAC `orderId|paymentId` with demo secret → `paymentService.verify`  
- Production: open Razorpay Checkout with `razorpayKeyId`; use signature from gateway (never ship live secrets in SPA)

### 7. Loading states

- Room details skeleton  
- `BookingLoadingScreen` for order prep / redirects  
- `LinearProgress` during pay  
- Disable Pay button while `paying`

### 8. Error handling

- Field errors on forms  
- Alert banners for API failures  
- Dedicated **failure route** so users can retry without losing booking id  

### 9. Optimistic UI

We do **not** mark the booking paid before verify succeeds.  
Optimistic UI would show “Paid!” then rollback — risky for money. Safer: progress bar + redirect only after `SUCCESS`.

### 10. Responsive booking pages

- `BookingStepper` → vertical on `sm`  
- Stack buttons column on mobile  
- `BookingLayout` padding scales with breakpoints  

### 11. Enterprise booking architecture

```
Pages → Components → Hooks → Services (Axios) → Spring Boot APIs
         ↑
   BookingWizardContext (draft only)
```

After `POST /bookings`, **server booking id** is the source of truth (payment/success load by id).

### 12. Common interview angles

See “Interview Preparation” at the end of Module 12 completion notes (questions only).

---

## Routes

| Path | Page | Auth |
|------|------|------|
| `/book?roomId&checkIn&checkOut&guests` | Multi-step wizard | Private |
| `/book/payment/:bookingId` | Payment | Private |
| `/book/success/:bookingId` | Success + invoice | Private |
| `/book/failure/:bookingId` | Failure + retry | Private |
| `/checkout/:bookingId` | Redirect → payment (Module 11 compat) | Private |

---

## Folder map (Module 12)

```
frontend/src/
├── pages/booking/          # Route screens for wizard, pay, success, failure
├── components/booking/     # Stepper, forms, summary, price, payment panel, loading
├── hooks/                  # useBookingWizard, useBookingPrice, usePaymentCheckout
├── services/               # bookingService, guestService, paymentService (extended)
├── context/                # BookingWizardContext (draft state)
├── layouts/                # BookingLayout (stepper shell)
└── utils/                  # bookingValidation, priceCalculation, mockPaymentSign
```

| Folder | Why it exists |
|--------|----------------|
| `pages/booking/` | One file per URL step; lazy-load payment/success/failure |
| `components/booking/` | Reusable step UI tested in isolation |
| `hooks/` | Encapsulate price math & payment orchestration |
| `services/` | Thin Axios wrappers — no JSX |
| `context/` | Share draft across wizard steps without prop drilling |
| `layouts/` | Consistent stepper chrome |
| `utils/` | Pure functions — easiest unit tests |

---

## Booking flow (end-to-end)

```
Visitor → Hotel Search (/rooms) → Room Details → Book Now
  → Login (if needed) → Booking Form → Guest Details → Summary
  → POST /bookings → Payment (create-order + verify)
  → Success (invoice) | Failure (retry)
```

1. **Search** — Module 11 filters inventory  
2. **Details** — deep link includes dates/guests in query  
3. **Book Now** — requires JWT; return URL preserved  
4. **Booking Form** — dates/capacity/availability  
5. **Guest** — upsert Guest by email  
6. **Summary** — create booking  
7. **Payment** — order + verify  
8. **Success** — confirmation + PDF invoice  

---

## APIs reused (unchanged)

- `GET /rooms/{id}`  
- `GET /bookings/availability`  
- `POST /bookings`  
- `GET /bookings/{id}`  
- `GET|POST|PUT /guests…`  
- `POST /payments/create-order`  
- `POST /payments/verify`  
- `GET /payments/invoice/pdf/{bookingId}`  

---

## Run & test

```bash
cd frontend
npm test
npm run build
```

---

## Address field note

Guest API has no `address` column. The UI collects address and appends it into `specialRequests` when creating the booking — honest UI without inventing backend fields.

---

## Practice solutions (implemented)

| Exercise | Solution in code |
|----------|------------------|
| (1) Nights live label | `BookingFormStep` + `data-testid="nights-live-label"` via `nightsBetween(watch(...))` |
| (2) Block Continue if unavailable | `availabilityOk === false` disables submit + warning Alert |
| (3) Tax rate tooltip | `PriceBreakdown` MUI `Tooltip` on GST row (`TAX_RATE`) |
| (4) Persist draft | `wizardDraftStorage.js` ↔ `BookingWizardContext` `sessionStorage` |
| (5) Edit guest | Summary `Edit guest` → `setStep(1)` |

| Coding task | Solution |
|-------------|----------|
| Zod + resolvers | `utils/bookingSchemas.js` + `zodResolver` on stay/guest forms |
| Split PaymentPanel | `OrderSummary.jsx` + `PayButton.jsx` composed by `PaymentPanel` |
| Playwright E2E | `e2e/booking-happy-path.spec.js` (API mocked) — `npm run test:e2e` |

| Challenge | Solution |
|-----------|----------|
| 2 rooms / `roomIds[]` | `RoomMultiSelect` (max 2), context `roomIds`/`rooms`, summary lines + `POST /bookings` with `roomIds` |

```bash
cd frontend
npm test
npm run test:e2e   # installs Chromium on first run if needed
```
