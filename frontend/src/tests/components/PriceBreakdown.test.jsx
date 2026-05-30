import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import PriceBreakdown from '../../components/booking/PriceBreakdown';
import { renderWithProviders } from '../testUtils';

/**
 * Practice #5 — tax line is visible when a price estimate is provided.
 */
describe('PriceBreakdown (practice)', () => {
  it('shows tax (GST) line when estimate is present', () => {
    const price = {
      lines: [],
      roomCharges: 10000,
      nights: 2,
      roomCount: 1,
      discount: 0,
      taxes: 1800,
      serviceCharges: 0,
      grandTotal: 11800,
    };

    renderWithProviders(<PriceBreakdown price={price} />);

    expect(screen.getByTestId('price-breakdown')).toBeInTheDocument();
    expect(screen.getByText(/Taxes \(GST est\.\)/i)).toBeInTheDocument();
    expect(screen.getByTestId('tax-rate-tooltip')).toBeInTheDocument();
  });
});
