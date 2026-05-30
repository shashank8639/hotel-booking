import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import OrderSummary from '../../components/booking/OrderSummary';
import { renderWithProviders } from '../testUtils';

describe('OrderSummary', () => {
  it('shows booking and payment order lines', () => {
    renderWithProviders(
      <OrderSummary
        booking={{ totalAmount: 9900 }}
        order={{
          paymentId: 15,
          razorpayOrderId: 'order_mock_1',
          amount: 9900,
          status: 'PENDING',
        }}
      />
    );

    expect(screen.getByTestId('order-summary')).toBeInTheDocument();
    expect(screen.getByText('Booking total')).toBeInTheDocument();
    expect(screen.getByText('order_mock_1')).toBeInTheDocument();
    expect(screen.getByText('PENDING')).toBeInTheDocument();
  });
});
