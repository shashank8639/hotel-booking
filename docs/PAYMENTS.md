# Module 7 Guide — Payment Management (Learn by Building)

Payments turn a **PENDING booking** into a **CONFIRMED** stay. This module integrates Razorpay-style checkout, webhooks, refunds, invoices, and receipts — without adding Maven libraries (pom locked).

---

## 1. Why payments are a separate module

| Layer | Owns |
|-------|------|
| Booking | Who / when / which rooms / how much owed (net room total) |
| Payment | Collecting money + GST, gateway IDs, refunds, invoices, FX |

Never put card numbers in your DB. The gateway (Razorpay) handles PCI-sensitive data; you store **references** (`order_id`, `payment_id`) and status.

---

## 2. Payment flow

```
Booking (PENDING)  net = Σ room subtotals
   → POST /payments/create-order
        taxable = net
        gst = taxable × 18%     (EXCLUSIVE model)
        charge = taxable + gst
        optional currency → FX snapshot → amount_in_base
        Payment PENDING + expires_at
   → Frontend Razorpay Checkout
   → POST /payments/verify      (HMAC; idempotent if already SUCCESS)
   → Payment SUCCESS + invoice + async email
   → Booking → CONFIRMED
```

Webhook backup: `POST /payments/webhook` (public; signature auth). Replay + concurrent retry storms are absorbed via unique `event_id`.

---

## 3. Stretch checklist (Part F)

| Task | Status | Where |
|------|--------|--------|
| Idempotent verify | Done | `verifyPayment` returns SUCCESS if same payment id |
| Expire PENDING orders | Done | `expires_at` + `PendingPaymentExpiryService` (V9) |
| GST exclusive model | Done | charge = taxable + GST; invoice `taxModel=EXCLUSIVE` |
| Admin-only refunds | Done | `POST /payments/refund` → `ROLE_ADMIN` + `@PreAuthorize` |
| Webhook retry storm | Done | catch `DataIntegrityViolationException` on event insert |
| `signPayment` test-only | Done | `TestPaymentSignController` `@Profile("test")` |
| Payment attempt audit | Done | `payment_attempts` / `PaymentAttempt` |
| Multi-currency + FX snapshot | Done | `currency`, `fxRate`, `amountInBase`, `FxRateService` |

---

## 4. Guided tour

1. `payment/RazorpaySignatureUtil.java` — HMAC SHA256  
2. `payment/MockRazorpayGateway.java` / `LiveRazorpayGateway.java`  
3. `service/impl/PaymentServiceImpl.java` — order, verify, refund, webhook, audit  
4. `service/impl/InvoiceServiceImpl.java` — exclusive GST + PDF  
5. `controller/PaymentController.java` + `TestPaymentSignController` (test profile)  
6. `V6` + `V9__payment_module7_practice.sql`  

---

## 5. Status lifecycle

```
PENDING → SUCCESS → REFUNDED (full)
PENDING → FAILED
PENDING → CANCELLED (expiry job / abandon)
```

---

## 6. Signature (interview gold)

Checkout verify:

```text
HMAC_SHA256(key_secret, orderId + "|" + paymentId)
```

Webhook: HMAC of **raw body**. Store `event_id` to block replays; unique constraint + catch race for retry storms.

---

## 7. Try this

1. Create booking → create-order (amount = net × 1.18) → verify (twice → idempotent)  
2. `GET /payments/invoice/{bookingId}` — `taxModel=EXCLUSIVE`  
3. Refund as CUSTOMER → **403**; as ADMIN → **200**  
4. Replay webhook `id` → ignored; parallel insert race → absorbed  
5. `mvn test -Dtest=PaymentServiceTest,PaymentControllerTest,InvoiceServiceTest,PendingPaymentExpiryServiceTest`

```bash
mysql -h 127.0.0.1 -u root -p hotel_booking \
  < backend/src/main/resources/db/migration/V9__payment_module7_practice.sql
```

---

## 8. Security

- `POST /payments/webhook` → permitAll  
- `POST /payments/refund` → **ADMIN only**  
- `/payments/**` → ADMIN or CUSTOMER  
- `POST /payments/test/sign` → only when `spring.profiles.active=test`

---

## 9. What’s next

Current path: [MODULES.md](MODULES.md)
