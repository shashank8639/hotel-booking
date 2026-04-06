# Module 4 Guide — Guest Management (Learn by Building)

This guide teaches you Guest Management the way a senior mentor would walk you through it on the job.

You already have:
- Tables & `Guest` entity (Module 2)
- Security filter chain (Module 3)

Now you build the **first complete feature**: create, read, update, delete, and search guests.

---

## 1. Start here — what problem are we solving?

Imagine a hotel reception desk.

Staff need to:
- Register a new guest  
- Look up a guest by email or phone when they call  
- Search by name when spelling is unclear  
- Update phone/email  
- Remove a guest who was created by mistake (only if they have no bookings)

That entire desk workflow is **Guest Management**.

In software terms: a **CRUD API** + **search** + **validation** + **pagination**.

---

## 2. Mental model — five layers (memorize this)

Before opening files, hold this picture:

```
HTTP request (JSON)
        ↓
┌───────────────────┐
│  Controller       │  Speaks HTTP. Thin. No business rules.
└─────────┬─────────┘
          ↓
┌───────────────────┐
│  Service          │  Business rules. Transactions. Decisions.
└─────────┬─────────┘
          ↓
┌───────────────────┐
│  Repository       │  Database questions only.
└─────────┬─────────┘
          ↓
┌───────────────────┐
│  Entity / MySQL   │  Persistent data.
└───────────────────┘
```

**DTOs** sit at the edge (request/response JSON).  
**Entity** sits at the database.  
Never send the Entity straight to the browser in production APIs.

---

## 3. Guided tour of the code (open these in order)

### Step A — The contract (what the API accepts/returns)

Open:
- `dto/GuestRequest.java`  
- `dto/GuestResponse.java`

**Ask yourself:**
- Why do we validate on `GuestRequest` and not on the entity?  
- What happens if email is invalid?

**Answer to check against:** Validation belongs at the API boundary so bad data never reaches the service/DB. Invalid input → `400` with field errors.

---

### Step B — The HTTP door

Open:
- `controller/GuestController.java`

Notice how short each method is. Example:

```java
@PostMapping
public ResponseEntity<GuestResponse> createGuest(@Valid @RequestBody GuestRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(guestService.createGuest(request));
}
```

**Mentor tip:** If a controller method is longer than a few lines, business logic is leaking in. Push it to the service.

**Endpoints you now own:**

| What staff want | Call this |
|-----------------|-----------|
| Add guest | `POST /api/guests` |
| Open guest card | `GET /api/guests/{id}` |
| Browse guest list | `GET /api/guests?page=0&size=10&sort=lastName,asc` |
| Correct details | `PUT /api/guests/{id}` |
| Remove guest | `DELETE /api/guests/{id}` |
| Find by email | `GET /api/guests/search/email?email=` |
| Find by phone | `GET /api/guests/search/phone?phone=` |
| Find by name | `GET /api/guests/search/name?name=` |

---

### Step C — Business rules (the real brain)

Open:
- `service/GuestService.java` (interface — the promise)  
- `service/impl/GuestServiceImpl.java` (the implementation)

**Business rules we enforce (read them in the code):**

1. Email must be unique  
2. Phone must be unique when provided  
3. You cannot delete a guest who still has bookings  

**Why in the service?**  
Repositories don’t know hotel policy. Controllers shouldn’t either. Services are where “the company rules” live.

**Also notice:**
- `@Transactional` on write methods  
- Class-level `readOnly = true` for safer default reads  
- Logging at INFO for create/update/delete, DEBUG for reads  

---

### Step D — Talking to the database

Open:
- `repository/GuestRepository.java`

Spring Data turns method names into SQL:

| Method | Meaning |
|--------|---------|
| `findByEmailIgnoreCase` | Exact email, case-insensitive |
| `findByPhone` | Exact phone |
| `existsByEmail` | Duplicate check without loading full row |
| `searchByName` | Partial match on first/last/full name (JPQL) |

**Mentor tip:** Keep repositories “dumb.” No `if` business rules here — only queries.

---

### Step E — Mapping Entity ↔ DTO

Open:
- `mapper/GuestMapper.java`

MapStruct generates the boring `setX(getX())` code at compile time.

**Why not map in the controller?**  
Mapping is conversion logic; keep controllers focused on HTTP.

---

### Step F — Errors that clients can understand

Open:
- `exception/GlobalExceptionHandler.java`  
- `GuestNotFoundException`, `DuplicateGuestException`, `GuestHasBookingsException`

| Situation | Exception | HTTP |
|-----------|-----------|------|
| Guest missing | `GuestNotFoundException` | 404 |
| Email/phone taken | `DuplicateGuestException` | 409 |
| Has bookings | `GuestHasBookingsException` | 409 |
| Bad JSON fields | validation | 400 |

**Mentor tip:** Controllers throw (or services throw); one global handler formats JSON. Don’t scatter `try/catch` in every controller method.

---

## 4. Follow one request end-to-end (Create Guest)

Say the client sends:

```http
POST /api/guests
Content-Type: application/json
Authorization: Bearer <token>

{
  "firstName": "Rahul",
  "lastName": "Sharma",
  "email": "rahul.sharma@example.com",
  "phone": "+91-9876543210"
}
```

**What happens:**

1. **Security filter** (Module 3) checks JWT — request must be authenticated  
2. **Controller** receives body → `@Valid` runs Bean Validation  
3. If invalid → `GlobalExceptionHandler` → `400`  
4. **Service** checks email/phone not already used  
5. **Mapper** converts `GuestRequest` → `Guest` entity  
6. **Repository** `save()` → Hibernate `INSERT` → MySQL  
7. **Mapper** converts saved entity → `GuestResponse`  
8. Controller returns **`201 Created`** + JSON  

Draw this on paper once. If you can redraw it from memory, you understand the module.

---

## 5. CRUD in plain language

| Letter | Meaning | Our API | Typical status |
|--------|---------|---------|----------------|
| **C** | Create | `POST /guests` | 201 |
| **R** | Read | `GET /guests`, `GET /guests/{id}` | 200 |
| **U** | Update | `PUT /guests/{id}` | 200 |
| **D** | Delete | `DELETE /guests/{id}` | 204 |

**Extras that make it “production-ready” (not toy CRUD):**
- Validation  
- Pagination + sorting  
- Search  
- Conflict rules (409)  
- Unit tests  

---

## 6. Pagination & search — why receptionists care

**Pagination:**  
A hotel with 50,000 guests cannot dump everyone into one response.

```
GET /api/guests?page=0&size=10&sort=lastName,asc
```

**Search:**
- Email / phone → exact (unique identifiers)  
- Name → partial (`LIKE %pat%`) because humans type imperfectly  

---

## 7. Try this (practice, not optional)

Do these yourself:

1. **Break validation** — POST a guest with `"email": "not-an-email"`. Confirm you get `400` and a field error.  
2. **Duplicate email** — create the same email twice. Confirm `409`.  
3. **Pagination** — call `GET /guests?page=0&size=2` after inserting 3+ guests.  
4. **Read the test** — open `GuestServiceTest` and explain one test method in your own words.  
5. **Explain out loud** — “Why is the controller thin?” for 60 seconds.  

Run tests:

```bash
cd backend
mvn test -Dtest=GuestRepositoryTest,GuestServiceTest,GuestControllerTest
```

---

## 8. Security note (honest)

`/guests/**` requires authentication (see [SECURITY.md](SECURITY.md)).

Login/register HTTP APIs are **not finished yet**, so calling Guest APIs from Postman needs a real JWT once Auth endpoints exist. Unit tests bypass security filters on purpose so you can test Guest logic in isolation.

---

## 9. Interview warmup (answer without looking)

1. Why DTO instead of returning `Guest` entity?  
2. Why `409` for duplicate email instead of `400`?  
3. Why delete is blocked when bookings exist?  
4. What does `@Transactional` do on `createGuest`?  
5. Difference between `GET /guests/{id}` and search-by-email?  

(Write answers in a notebook. Then check against the concepts Q&A from the mentor chat.)

---

## 10. What you should feel confident about after this module

You can:
- Design a layered CRUD feature  
- Place validation, business rules, and queries in the right layer  
- Return correct HTTP statuses  
- Paginate and search like a real admin API  
- Write repository/service/controller tests  

**Next chapter:** Room Management — same pattern, new domain (`Room`).

Back to the learning path: [MODULES.md](MODULES.md)
