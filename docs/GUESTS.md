# Guest Management — Module 4

## Status

| Item | Status |
|------|--------|
| GuestRepository (email/phone/name search) | Done |
| GuestService + GuestServiceImpl | Done |
| GuestController (CRUD + search) | Done |
| GuestRequest / GuestResponse DTOs | Done |
| GuestMapper (MapStruct) | Done |
| GlobalExceptionHandler | Done |
| Unit tests (repo, service, controller) | Done |

## API Endpoints

Base path: `/api/guests` (context-path `/api` from `application.yml`).

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| POST | `/guests` | Create guest | 201 |
| GET | `/guests/{id}` | Get by ID | 200 / 404 |
| GET | `/guests` | List (pagination + sorting) | 200 |
| PUT | `/guests/{id}` | Update guest | 200 / 404 / 409 |
| DELETE | `/guests/{id}` | Delete guest | 204 / 404 / 409 |
| GET | `/guests/search/email?email=` | Search by email | 200 / 404 |
| GET | `/guests/search/phone?phone=` | Search by phone | 200 / 404 |
| GET | `/guests/search/name?name=` | Partial name search | 200 |

### Pagination query params

```
GET /api/guests?page=0&size=10&sort=lastName,asc
```

## Request body example

```json
{
  "firstName": "Rahul",
  "lastName": "Sharma",
  "email": "rahul.sharma@example.com",
  "phone": "+91-9876543210"
}
```

## Key classes

| Class | Package | Purpose |
|-------|---------|---------|
| `GuestController` | controller | Thin REST layer |
| `GuestService` / `GuestServiceImpl` | service | Business rules, duplicates, delete guard |
| `GuestRepository` | repository | Derived + JPQL search queries |
| `GuestRequest` / `GuestResponse` | dto | API contract + Bean Validation |
| `GuestMapper` | mapper | Entity ↔ DTO (MapStruct) |
| `GlobalExceptionHandler` | exception | 400 / 404 / 409 JSON errors |

## Business rules

1. Email must be unique (create & update).
2. Phone must be unique when provided.
3. Guest cannot be deleted if bookings exist (`GuestHasBookingsException` → 409).
4. Validation runs at the API boundary (`@Valid` on request body).

## Security note

`/guests/**` requires authentication (see [SECURITY.md](SECURITY.md)). Call with:

```
Authorization: Bearer <access-token>
```

Auth login/register endpoints are not wired yet — use Swagger Authorize once Auth APIs exist, or disable filters only in tests.

## Tests

```bash
cd backend
mvn test -Dtest=GuestRepositoryTest,GuestServiceTest,GuestControllerTest
```

See [MODULES.md](MODULES.md) for the full learning path.
