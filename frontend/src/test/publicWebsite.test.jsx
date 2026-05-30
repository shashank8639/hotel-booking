import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material';
import { SearchBar } from '../components/home/SearchBar';
import { RoomCard } from '../components/rooms/RoomCard';
import { validateSearchState, parseSearchParams, toSearchParams } from '../utils/searchParams';
import { nightsBetween, formatCurrency } from '../utils/format';

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => vi.fn(),
  };
});

const theme = createTheme();

function wrap(ui) {
  return render(
    <ThemeProvider theme={theme}>
      <MemoryRouter>{ui}</MemoryRouter>
    </ThemeProvider>
  );
}

describe('search utils', () => {
  it('rejects checkout before check-in', () => {
    const errors = validateSearchState({
      checkIn: '2026-08-10',
      checkOut: '2026-08-09',
      guests: 2,
    });
    expect(errors.checkOut).toBeTruthy();
  });

  it('round-trips search params', () => {
    const state = {
      location: 'Mumbai',
      checkIn: '2026-08-10',
      checkOut: '2026-08-12',
      guests: 2,
      roomType: 'DELUXE',
      minPrice: '',
      maxPrice: '',
      status: 'AVAILABLE',
      sort: 'pricePerNight,asc',
      page: 0,
      size: 6,
    };
    const parsed = parseSearchParams(toSearchParams(state));
    expect(parsed.roomType).toBe('DELUXE');
    expect(parsed.guests).toBe(2);
  });

  it('computes nights', () => {
    expect(nightsBetween('2026-08-10', '2026-08-12')).toBe(2);
  });
});

describe('SearchBar', () => {
  it('renders search CTA', () => {
    wrap(<SearchBar compact />);
    expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
  });

  it('shows validation when checkout invalid', () => {
    wrap(<SearchBar initialValues={{ checkIn: '2026-08-10', checkOut: '2026-08-09', guests: 2 }} />);
    fireEvent.click(screen.getByRole('button', { name: /search/i }));
    expect(screen.getByText(/check-out must be after check-in/i)).toBeInTheDocument();
  });
});

describe('RoomCard', () => {
  it('renders room type, price and book CTA', () => {
    const room = {
      id: 7,
      roomNumber: '101',
      roomType: 'DELUXE',
      capacity: 2,
      pricePerNight: 4500,
      effectivePrice: 4500,
      status: 'AVAILABLE',
      images: [],
    };
    wrap(<RoomCard room={room} />);
    expect(screen.getByTestId('room-card')).toBeInTheDocument();
    expect(screen.getByText(/DELUXE/i)).toBeInTheDocument();
    expect(screen.getByText(formatCurrency(4500), { exact: false })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /view & book/i })).toHaveAttribute(
      'href',
      expect.stringContaining('/rooms/7')
    );
  });
});
