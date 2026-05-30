import { describe, it, expect, vi, beforeAll, afterAll, afterEach, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material';
import { setupServer } from 'msw/node';
import AdminLayout from '../layouts/AdminLayout';
import AdminDashboardHomePage from '../pages/admin/AdminDashboardHomePage';
import AdminRoomsPage from '../pages/admin/AdminRoomsPage';
import AdminBookingsPage from '../pages/admin/AdminBookingsPage';
import AdminDataTable, { toggleSort, parseSort } from '../components/admin/AdminDataTable';
import AdminEmptyState from '../components/admin/AdminEmptyState';
import { loadReportRange, saveReportRange, REPORT_RANGE_KEY } from '../utils/reportRangeStorage';
import { adminDashboardHandler } from '../mocks/handlers';
import { useTodaysCheckInsPoll } from '../hooks/useTodaysCheckInsPoll';
import { adminReportService } from '../services/adminReportService';

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    isAuthenticated: true,
    user: { firstName: 'Ada', lastName: 'Admin' },
    roles: ['ADMIN'],
    logout: vi.fn(),
  }),
}));

vi.mock('../services/adminReportService', () => ({
  adminReportService: {
    getDashboard: vi.fn(),
    getRevenue: vi.fn().mockResolvedValue({ series: [], byRoomType: [] }),
    getOccupancy: vi.fn().mockResolvedValue({ dailyOccupancy: [] }),
    getBookings: vi.fn().mockResolvedValue({ totalBookings: 0, byStatus: [], byCheckInDate: [] }),
    getPayments: vi.fn().mockResolvedValue({ byStatus: [] }),
    getMonthly: vi.fn().mockResolvedValue({ year: 2026, month: 8, monthlyGuestRegistrations: 0 }),
  },
}));

vi.mock('../services/roomService', () => ({
  roomService: {
    search: vi.fn().mockResolvedValue({
      content: [],
      totalElements: 0,
      number: 0,
      size: 10,
    }),
    getImages: vi.fn().mockResolvedValue([]),
  },
}));

vi.mock('../services/adminRoomService', () => ({
  adminRoomService: {
    create: vi.fn(),
    update: vi.fn(),
    remove: vi.fn(),
    updateAvailability: vi.fn(),
  },
}));

vi.mock('../services/bookingService', () => ({
  bookingService: {
    list: vi.fn().mockResolvedValue({
      content: [
        {
          id: 1,
          guestFirstName: 'A',
          guestLastName: 'B',
          checkInDate: '2026-08-10',
          checkOutDate: '2026-08-12',
          status: 'CANCELLED',
          totalAmount: 1000,
        },
      ],
      totalElements: 1,
      number: 0,
      size: 10,
    }),
    listByStatus: vi.fn(),
    getById: vi.fn(),
    cancel: vi.fn(),
    updateStatus: vi.fn(),
  },
}));

const theme = createTheme();
const server = setupServer(adminDashboardHandler);

beforeAll(() => server.listen({ onUnhandledRequest: 'bypass' }));
afterEach(() => {
  server.resetHandlers();
  sessionStorage.clear();
  vi.clearAllMocks();
});
afterAll(() => server.close());

function renderAdmin(ui, path = '/admin/dashboard') {
  return render(
    <ThemeProvider theme={theme}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/admin" element={<AdminLayout />}>
            <Route path="dashboard" element={ui} />
            <Route path="rooms" element={ui} />
            <Route path="bookings" element={ui} />
          </Route>
        </Routes>
      </MemoryRouter>
    </ThemeProvider>
  );
}

describe('AdminDataTable + sort helpers', () => {
  it('toggles sort direction', () => {
    expect(toggleSort('roomNumber', 'asc', 'roomNumber')).toBe('roomNumber,desc');
    expect(toggleSort('roomNumber', 'desc', 'capacity')).toBe('capacity,asc');
    expect(parseSort('pricePerNight,desc')).toEqual(['pricePerNight', 'desc']);
  });

  it('shows empty state when no rows', () => {
    render(
      <ThemeProvider theme={theme}>
        <AdminDataTable
          columns={[{ id: 'a', label: 'A' }]}
          rows={[]}
          emptyTitle="No rooms found"
          emptyDescription="Create a room"
        />
      </ThemeProvider>
    );
    expect(screen.getByTestId('admin-empty-state')).toHaveTextContent('No rooms found');
  });

  it('calls onSortChange from header', () => {
    const onSortChange = vi.fn();
    render(
      <ThemeProvider theme={theme}>
        <AdminDataTable
          columns={[{ id: 'roomNumber', label: 'Number', sortable: true }]}
          rows={[{ id: 1, roomNumber: '101' }]}
          sort="roomNumber,asc"
          onSortChange={onSortChange}
          renderCell={(c, r) => r[c.id]}
        />
      </ThemeProvider>
    );
    fireEvent.click(screen.getByRole('button', { name: /number/i }));
    expect(onSortChange).toHaveBeenCalledWith('roomNumber,desc');
  });
});

describe('report range sessionStorage', () => {
  it('saves and loads range', () => {
    saveReportRange({ startDate: '2026-01-01', endDate: '2026-01-31', year: 2026, month: 1 });
    expect(sessionStorage.getItem(REPORT_RANGE_KEY)).toBeTruthy();
    const loaded = loadReportRange({
      startDate: 'x',
      endDate: 'y',
      year: 2000,
      month: 2,
    });
    expect(loaded.startDate).toBe('2026-01-01');
    expect(loaded.month).toBe(1);
  });
});

describe('MSW /admin/dashboard handler', () => {
  it('returns mocked dashboard JSON', async () => {
    const res = await fetch('http://localhost/api/admin/dashboard');
    const body = await res.json();
    expect(body.todaysCheckIns).toBe(5);
    expect(body.monthlyRevenue).toBe(200000);
  });
});

describe('dashboard refresh + empty rooms', () => {
  beforeEach(() => {
    adminReportService.getDashboard.mockResolvedValue({
      totalGuests: 1,
      totalRooms: 1,
      availableRooms: 1,
      occupiedRooms: 0,
      todaysBookings: 0,
      todaysCheckIns: 2,
      todaysRevenue: 0,
      monthlyRevenue: 0,
      pendingPayments: 0,
      recentBookings: [],
    });
  });

  it('renders refresh button and triggers reload', async () => {
    renderAdmin(<AdminDashboardHomePage />);
    expect(await screen.findByTestId('dashboard-refresh')).toBeInTheDocument();
    const callsBefore = adminReportService.getDashboard.mock.calls.length;
    fireEvent.click(screen.getByTestId('dashboard-refresh'));
    await waitFor(() => {
      expect(adminReportService.getDashboard.mock.calls.length).toBeGreaterThan(callsBefore);
    });
  });

  it('rooms page shows empty-state illustration', async () => {
    renderAdmin(<AdminRoomsPage />, '/admin/rooms');
    expect(await screen.findByTestId('admin-empty-state')).toBeInTheDocument();
    expect(screen.getByText(/no rooms found/i)).toBeInTheDocument();
  });
});

describe('cancelled booking highlight', () => {
  it('marks cancelled rows', async () => {
    renderAdmin(<AdminBookingsPage />, '/admin/bookings');
    expect(await screen.findByTestId('cancelled-booking-chip')).toBeInTheDocument();
  });
});

describe('useTodaysCheckInsPoll abortable', () => {
  it('loads check-ins and cleans up without throwing', async () => {
    adminReportService.getDashboard.mockResolvedValue({ todaysCheckIns: 7 });

    function Probe() {
      const { todaysCheckIns } = useTodaysCheckInsPoll(true, 60_000);
      return <div data-testid="checkins">{todaysCheckIns ?? 'null'}</div>;
    }

    const { unmount } = render(
      <ThemeProvider theme={theme}>
        <Probe />
      </ThemeProvider>
    );

    await waitFor(() => expect(screen.getByTestId('checkins')).toHaveTextContent('7'));
    act(() => {
      unmount();
    });
    expect(adminReportService.getDashboard).toHaveBeenCalled();
  });
});

describe('AdminEmptyState', () => {
  it('renders title', () => {
    render(
      <ThemeProvider theme={theme}>
        <AdminEmptyState title="Empty" description="None" />
      </ThemeProvider>
    );
    expect(screen.getByTestId('admin-empty-state')).toHaveTextContent('Empty');
  });
});
