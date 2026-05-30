import { test, expect } from '@playwright/test';

/**
 * E2E practice challenge:
 * Login as customer → search rooms → open booking wizard smoke (payment mocked).
 */

const room = {
  id: 1,
  roomNumber: '101',
  roomType: 'DELUXE',
  capacity: 3,
  pricePerNight: 5000,
  effectivePrice: 5000,
  status: 'AVAILABLE',
};

async function mockApis(page) {
  await page.route('**/api/**', async (route) => {
    const req = route.request();
    const url = new URL(req.url());
    const path = url.pathname.replace(/^\/api/, '');
    const method = req.method();

    const json = (body, status = 200) =>
      route.fulfill({
        status,
        contentType: 'application/json',
        body: JSON.stringify(body),
      });

    if (path === '/auth/login' && method === 'POST') {
      return json({
        accessToken: 'e2e-access',
        refreshToken: 'e2e-refresh',
        tokenType: 'Bearer',
        expiresIn: 3600000,
        user: {
          id: 1,
          email: 'customer@example.com',
          firstName: 'Cust',
          lastName: 'Omer',
          roles: ['CUSTOMER'],
        },
      });
    }
    if (path === '/auth/me' && method === 'GET') {
      return json({
        id: 1,
        email: 'customer@example.com',
        firstName: 'Cust',
        lastName: 'Omer',
        roles: ['CUSTOMER'],
      });
    }
    if (path === '/rooms/search' && method === 'GET') {
      return json({ content: [room], totalElements: 1, totalPages: 1, number: 0 });
    }
    if (path === '/rooms/1' && method === 'GET') return json(room);
    if (path.startsWith('/bookings/availability')) {
      return json({ rooms: [{ roomId: 1, available: true }] });
    }
    if (path === '/guests/search/email' && method === 'GET') {
      return json({
        id: 7,
        firstName: 'Cust',
        lastName: 'Omer',
        email: 'customer@example.com',
        phone: '+91 9876543210',
      });
    }
    if (path.startsWith('/guests/') && method === 'PUT') {
      return json({ id: 7, ...JSON.parse(req.postData() || '{}') });
    }
    if (path === '/guests' && method === 'POST') {
      return json({ id: 7, ...JSON.parse(req.postData() || '{}') }, 201);
    }
    if (path === '/bookings' && method === 'POST') {
      const body = JSON.parse(req.postData() || '{}');
      return json(
        {
          id: 55,
          guestId: body.guestId,
          guestFirstName: 'Cust',
          guestLastName: 'Omer',
          guestEmail: 'customer@example.com',
          checkInDate: body.checkInDate,
          checkOutDate: body.checkOutDate,
          numberOfNights: 2,
          totalAmount: 10000,
          status: 'PENDING',
          rooms: [{ roomId: 1, roomNumber: '101', pricePerNight: 5000 }],
        },
        201
      );
    }
    if (path === '/payments/create-order' && method === 'POST') {
      return json({
        paymentId: 1,
        bookingId: 55,
        razorpayOrderId: 'order_mock_e2e',
        amount: 10000,
        status: 'CREATED',
      });
    }
    if (path === '/payments/verify' && method === 'POST') {
      return json({ id: 1, status: 'SUCCESS', bookingId: 55 });
    }
    if (path === '/bookings/55' && method === 'GET') {
      return json({
        id: 55,
        guestFirstName: 'Cust',
        guestLastName: 'Omer',
        guestEmail: 'customer@example.com',
        checkInDate: '2026-08-10',
        checkOutDate: '2026-08-12',
        numberOfNights: 2,
        totalAmount: 10000,
        status: 'CONFIRMED',
        rooms: [{ roomNumber: '101', pricePerNight: 5000 }],
      });
    }

    return json({ message: `Unmocked ${method} ${path}` }, 404);
  });
}

test('login → search rooms → booking wizard smoke', async ({ page }) => {
  await mockApis(page);

  // 1) Login as customer
  await page.goto('/login');
  await page.getByLabel(/email/i).fill('customer@example.com');
  await page.locator('#login-password').fill('password123');
  await page.getByRole('button', { name: /login/i }).click();

  // 2) Search rooms (public list)
  await page.goto('/rooms?checkIn=2026-08-10&checkOut=2026-08-12&guests=2');
  await expect(page.getByTestId('room-card').first()).toBeVisible({ timeout: 15_000 });
  await page.getByRole('link', { name: /view & book/i }).first().click();

  // 3) Enter booking wizard from details (or deep-link if details CTA differs)
  await page.goto('/book?roomId=1&checkIn=2026-08-10&checkOut=2026-08-12&guests=2');
  await expect(page.getByTestId('nights-live-label')).toContainText('2 night');
  await page.getByTestId('continue-guest').click();

  await page.getByLabel(/phone number/i).fill('+91 9876543210');
  await page.getByRole('button', { name: /continue to summary/i }).click();

  await expect(page.getByTestId('price-breakdown')).toBeVisible();
  await page.getByTestId('confirm-booking').click();

  // Mocked payment path
  await expect(page.getByTestId('pay-now')).toBeVisible({ timeout: 15_000 });
  await page.getByTestId('pay-now').click();
  await expect(page.getByTestId('booking-success')).toBeVisible({ timeout: 15_000 });
});
