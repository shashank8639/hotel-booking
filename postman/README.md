# Postman

1. Import `Hotel_Booking_API.postman_collection.json`
2. Import `Hotel_Booking_Local.postman_environment.json` and select it
3. Run **Auth → Login** (or Register first) — tests script saves `accessToken`
4. Collection auth uses Bearer `{{accessToken}}`
5. For prod profile, set `baseUrl` to `http://localhost:8080/api/v1`

Admin report requests need a user with `ROLE_ADMIN`.
