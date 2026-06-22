import { http, HttpResponse } from 'msw';

/**
 * MSW handlers for admin APIs — use in Vitest / Storybook / local mocks.
 * Does not change Spring Boot contracts.
 */
export const adminDashboardHandler = http.get('*/api/admin/dashboard', () =>
  HttpResponse.json({
    totalGuests: 40,
    totalRooms: 20,
    availableRooms: 12,
    occupiedRooms: 8,
    todaysBookings: 3,
    todaysCheckIns: 5,
    todaysCheckOuts: 2,
    todaysRevenue: 15000,
    monthlyRevenue: 200000,
    pendingPayments: 2,
    completedPayments: 28,
    cancelledBookings: 1,
    recentBookings: [
      {
        id: 1,
        guestName: 'Ada Lovelace',
        checkInDate: '2026-08-10',
        checkOutDate: '2026-08-12',
        status: 'CONFIRMED',
        totalAmount: 10000,
      },
    ],
  })
);

export const handlers = [adminDashboardHandler];
