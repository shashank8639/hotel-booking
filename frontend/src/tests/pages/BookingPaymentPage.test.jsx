import { describe, it, expect, vi } from 'vitest';
import { screen } from '@testing-library/react';
import { Route, Routes } from 'react-router-dom';
import { renderWithProviders } from '../testUtils';

vi.mock('../../hooks/usePaymentCheckout', () => ({
  usePaymentCheckout: () => ({
    booking: {
      id: 1,
      totalAmount: 5000,
      status: 'PENDING',
      numberOfNights: 2,
      checkInDate: '2026-08-10',
      checkOutDate: '2026-08-12',
    },
    order: {
      paymentId: 9,
      razorpayOrderId: 'order_mock_9',
      amount: 5000,
      status: 'PENDING',
    },
    loading: false,
    paying: false,
    progress: 0,
    error: null,
    setError: vi.fn(),
    pay: vi.fn(),
  }),
}));

import BookingPaymentPage from '../../pages/booking/BookingPaymentPage';

describe('BookingPaymentPage', () => {
  it('renders payment panel for a pending booking', () => {
    renderWithProviders(
      <Routes>
        <Route path="/book/payment/:bookingId" element={<BookingPaymentPage />} />
      </Routes>,
      { route: '/book/payment/1' }
    );

    expect(screen.getByText(/order_mock_9/i)).toBeInTheDocument();
  });
});
