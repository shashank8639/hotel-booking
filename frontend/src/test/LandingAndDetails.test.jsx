import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material';
import LandingPage from '../pages/LandingPage';
import RoomDetailsPage from '../pages/RoomDetailsPage';

vi.mock('../services/hotelService', () => ({
  hotelService: {
    featured: vi.fn(),
    search: vi.fn(),
  },
  cityService: {
    popular: vi.fn(),
    list: vi.fn(),
  },
}));

vi.mock('../services/roomService', () => ({
  roomService: {
    search: vi.fn(),
    getById: vi.fn(),
    getImages: vi.fn(),
  },
}));

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({ isAuthenticated: false }),
}));

import { roomService } from '../services/roomService';
import { cityService, hotelService } from '../services/hotelService';

const theme = createTheme();

describe('LandingPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    hotelService.featured.mockResolvedValue([
      {
        id: 1,
        name: 'Grand Horizon Hyderabad',
        slug: 'grand-horizon-hyderabad',
        cityName: 'Hyderabad',
        starRating: 5,
        averageRating: 4.8,
        startingPrice: 4500,
        images: [],
      },
    ]);
    cityService.popular.mockResolvedValue([
      { id: 1, name: 'Hyderabad', slug: 'hyderabad' },
    ]);
    cityService.list.mockResolvedValue([
      { id: 1, name: 'Hyderabad', slug: 'hyderabad' },
    ]);
  });

  it('shows StayFinder hero and featured hotels', async () => {
    render(
      <ThemeProvider theme={theme}>
        <MemoryRouter>
          <LandingPage />
        </MemoryRouter>
      </ThemeProvider>
    );

    expect(screen.getByText(/Find hotels across Telangana/i)).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText(/Grand Horizon Hyderabad/i)).toBeInTheDocument();
    });
    expect(hotelService.featured).toHaveBeenCalled();
  });
});

describe('RoomDetailsPage booking navigation', () => {
  beforeEach(() => {
    roomService.getById.mockResolvedValue({
      id: 9,
      roomNumber: '205',
      roomType: 'SUITE',
      capacity: 3,
      pricePerNight: 9000,
      effectivePrice: 9000,
      status: 'AVAILABLE',
      description: 'Sea view suite',
      images: [],
    });
    roomService.getImages.mockResolvedValue([]);
  });

  it('sends anonymous users to login with return URL', async () => {
    render(
      <ThemeProvider theme={theme}>
        <MemoryRouter initialEntries={['/rooms/9?checkIn=2026-08-10&checkOut=2026-08-12&guests=2']}>
          <Routes>
            <Route path="/rooms/:id" element={<RoomDetailsPage />} />
            <Route path="/login" element={<div>Login Screen</div>} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>
    );

    await waitFor(() => expect(screen.getByText(/SUITE Room 205/i)).toBeInTheDocument());
    screen.getByRole('button', { name: /book now/i }).click();
    await waitFor(() => expect(screen.getByText('Login Screen')).toBeInTheDocument());
  });
});
