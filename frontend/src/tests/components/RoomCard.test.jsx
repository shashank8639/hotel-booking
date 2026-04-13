import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import { RoomCard } from '../../components/rooms/RoomCard';
import { renderWithProviders } from '../testUtils';

describe('RoomCard', () => {
  const room = {
    id: 7,
    roomNumber: '205',
    roomType: 'DELUXE',
    capacity: 2,
    pricePerNight: 4500,
    effectivePrice: 4000,
    status: 'AVAILABLE',
    description: 'City view deluxe room',
  };

  it('renders room number, type, and bookable price', () => {
    renderWithProviders(<RoomCard room={room} />);

    expect(screen.getByTestId('room-card')).toBeInTheDocument();
    expect(screen.getByText(/DELUXE · 205/)).toBeInTheDocument();
    expect(screen.getByText(/View & Book/i)).toBeInTheDocument();
  });
});
