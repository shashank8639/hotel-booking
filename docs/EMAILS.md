# Module 8 Guide — Email Notifications (Learn by Building)

Module 7 logged “emails.” Module 8 turns that into a **real notification subsystem**: HTML templates, attachments, async send, validation, outbox audit, and swappable mail/template engines.

---

## Constraint note

`pom.xml` stays free of Spring Mail / Thymeleaf jars by default. Swap implementations use **`@ConditionalOnClass` + reflection** so they compile today and activate when you add the jars later.

| Asked for | What we ship |
|-----------|----------------|
| Spring Mail | `EmailSender` + `LoggingEmailSender` (default). `JavaMailEmailSender` when `app.mail.transport=smtp` **and** `JavaMailSender` is on the classpath. |
| Thymeleaf | `PlaceholderEmailTemplateEngine` (default). `ThymeleafEmailTemplateEngine` when `app.mail.template-engine=thymeleaf` **and** Thymeleaf is on the classpath. |

---

## 1. Flow

```
Booking / Payment / Auth
   → AsyncNotificationFacade
   → NotificationServiceImpl
        → preference + bounce checks
        → EmailTemplateEngine (locale)
        → email_outbox row (PENDING)
        → EmailSender (log/.eml or SMTP)
        → outbox SENT / FAILED / SUPPRESSED
```

---

## 2. What gets emailed

| Event | Template | Notes |
|-------|----------|--------|
| Payment success | `payment-success.html` | Transactional |
| Booking confirmed | `booking-confirmation.html` | Transactional |
| Invoice + PDF | `invoice.html` | Transactional |
| Booking cancelled | `booking-cancellation.html` | **CC** `app.mail.ops-email` |
| Password reset | `password-reset.html` | Rate-limited |
| Marketing promo | `marketing-promo.html` | Opt-in only |

i18n: `templates/email/{locale}/…` with fallback to `templates/email/…` (Hindi cancel: `hi/booking-cancellation.html`).

---

## 3. Part F checklist

| Task | Status | Where |
|------|--------|--------|
| CC hotel ops on cancel | Done | `sendBookingCancellation` → `EmailMessage.cc` = `opsEmail` |
| i18n templates | Done | Locale folders + `guest.preferredLocale` |
| Bounce handling design | Done | `EmailBounceHandler` + `email_suppressions` + docs below |
| Rate-limit password reset | Done | `PasswordResetEmailRateLimiter` (default 3 / 60 min) |
| `email_outbox` table | Done | `V10` + `EmailOutbox` / `EmailOutboxService` |
| `JavaMailSender` `@ConditionalOnClass` | Done | `JavaMailEmailSender` |
| Thymeleaf swap | Done | Interface + `ThymeleafEmailTemplateEngine` |
| Guest prefs (marketing vs transactional) | Done | Guest columns + `sendMarketingPromo` |

Migration: `V10__email_module8_practice.sql`

---

## 4. Bounce handling design

```text
ESP webhook (SES/SendGrid bounce/complaint)
   → verify signature
   → EmailBounceHandler.recordBounce(email, HARD_BOUNCE|COMPLAINT, detail)
   → email_suppressions.active = true
   → future NotificationService.deliver() skips send, marks outbox SUPPRESSED
```

| Bounce type | Action |
|-------------|--------|
| Hard bounce | Suppress immediately |
| Soft bounce | Optional: suppress after N failures (extend later) |
| Complaint (spam) | Suppress + never market |

Transactional vs marketing: suppressions block **all** sends to that address; marketing additionally requires `marketingEmailsEnabled=true`.

---

## 5. Local demo

```bash
export APP_MAIL_ENABLED=true
export APP_MAIL_OUTBOX_DIRECTORY=target/email-outbox
export APP_MAIL_OPS_EMAIL=ops@grandhorizon.example
# cancel a booking → guest To + ops Cc in .eml
```

SMTP / Thymeleaf (when jars added):

```bash
export APP_MAIL_TRANSPORT=smtp
export APP_MAIL_TEMPLATE_ENGINE=thymeleaf
```

---

## 6. Try this

1. Cancel booking → `.eml` has `Cc: ops@…`  
2. Guest `preferredLocale=hi` → Hindi cancellation body  
3. Hit forgot-password 4× quickly → 4th suppressed by rate limit  
4. Opt-in marketing → `sendMarketingPromo` sends; default guest skips  
5. `mvn test -Dtest=NotificationServiceTest,EmailTemplateEngineTest,EmailSenderAndUtilsTest`

```bash
mysql -h 127.0.0.1 -u root -p hotel_booking \
  < backend/src/main/resources/db/migration/V10__email_module8_practice.sql
```

---

## 7. Guided tour

1. `NotificationServiceImpl` — prefs, CC, outbox, rate limit  
2. `PlaceholderEmailTemplateEngine` / `ThymeleafEmailTemplateEngine`  
3. `LoggingEmailSender` / `JavaMailEmailSender`  
4. `EmailBounceHandler`, `PasswordResetEmailRateLimiter`, `EmailOutboxService`  
5. `V10__email_module8_practice.sql`  
6. `AsyncNotificationFacade`

---

## 8. What’s next

**Module 9 — Reports & Admin Dashboard.** Path: [MODULES.md](MODULES.md)
