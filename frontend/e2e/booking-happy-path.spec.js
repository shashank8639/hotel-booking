import { test, expect } from '@playwright/test';

/**
 * Happy-path booking wizard with network mocks (no Spring Boot required).
 * Seeds auth sessionStorage so PrivateRoute allows /book.
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

const secondRoom = {
  id: 2,
  roomNumber: '102',
  roomType: 'STANDARD',
  capacity: 2,
  pricePerNight: 3000,
  effectivePrice: 3000,
  status: 'AVAILABLE',
};

async function seedAuth(page) {
  await page.addInitScript(() => {
    const user = JSON.stringify({
      id: 1,
      email: 'e2e@example.com',
      firstName: 'E2E',
      lastName: 'Tester',
      roles: ['CUSTOMER'],
    });
    sessionStorage.setItem('hb_access_token', 'e2e-access');
    sessionStorage.setItem('hb_refresh_token', 'e2e-refresh');
    sessionStorage.setItem('hb_auth_user', user);
    localStorage.setItem('hb_remember_me', 'false');
  });
}

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

    if (path === '/auth/me' && method === 'GET') {
      return json({
        id: 1,
        email: 'e2e@example.com',
        firstName: 'E2E',
        lastName: 'Tester',
        roles: ['CUSTOMER'],
      });
    }
    if (path === '/rooms/1' && method === 'GET') return json(room);
    if (path === '/rooms/search' && method === 'GET') {
      return json({ content: [room, secondRoom], totalElements: 2, totalPages: 1, number: 0 });
    }
    if (path.startsWith('/bookings/availability')) {
      return json({
        rooms: [
          { roomId: 1, available: true },
          { roomId: 2, available: true },
        ],
      });
    }
    if (path === '/guests/search/email' && method === 'GET') {
      return json({ id: 7, firstName: 'E2E', lastName: 'Tester', email: 'e2e@example.com', phone: '+91 9876543210' });
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
          id: 99,
          guestId: body.guestId,
          guestFirstName: 'E2E',
          guestLastName: 'Tester',
          guestEmail: 'e2e@example.com',
          checkInDate: body.checkInDate,
          checkOutDate: body.checkOutDate,
          numberOfNights: 2,
          totalAmount: 16000,
          status: 'PENDING',
          rooms: body.roomIds.map((id) => ({
            roomId: id,
            roomNumber: id === 1 ? '101' : '102',
            pricePerNight: id === 1 ? 5000 : 3000,
          })),
        },
        201
      );
    }
    if (path === '/payments/create-order' && method === 'POST') {
      return json({
        paymentId: 1,
        bookingId: 99,
        razorpayOrderId: 'order_mock_1001',
        amount: 16000,
        status: 'CREATED',
      });
    }
    if (path === '/payments/verify' && method === 'POST') {
      return json({ id: 1, status: 'SUCCESS', bookingId: 99 });
    }
    if (path === '/bookings/99' && method === 'GET') {
      return json({
        id: 99,
        guestFirstName: 'E2E',
        guestLastName: 'Tester',
        guestEmail: 'e2e@example.com',
        checkInDate: '2026-08-10',
        checkOutDate: '2026-08-12',
        numberOfNights: 2,
        totalAmount: 16000,
        status: 'CONFIRMED',
        rooms: [{ roomNumber: '101', pricePerNight: 5000 }],
      });
    }

    return json({ message: `Unmocked ${method} ${path}` }, 404);
  });
}

test('booking wizard happy path to success', async ({ page }) => {
  await seedAuth(page);
  await mockApis(page);

  await page.goto('/book?roomId=1&checkIn=2026-08-10&checkOut=2026-08-12&guests=2');

  await expect(page.getByTestId('nights-live-label')).toContainText('2 night');
  await page.getByTestId('continue-guest').click();

  await page.getByLabel(/phone number/i).fill('+91 9876543210');
  await page.getByRole('button', { name: /continue to summary/i }).click();

  await expect(page.getByTestId('price-breakdown')).toBeVisible();
  await expect(page.getByTestId('edit-guest')).toBeVisible();
  await page.getByTestId('confirm-booking').click();

  await expect(page.getByTestId('pay-now')).toBeVisible({ timeout: 15_000 });
  await page.getByTestId('pay-now').click();

  await expect(page.getByTestId('booking-success')).toBeVisible({ timeout: 15_000 });
  await expect(page.getByText(/#99/)).toBeVisible();
});
