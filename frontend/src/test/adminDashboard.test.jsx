import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material';
import AdminLayout from '../layouts/AdminLayout';
import { ADMIN_NAV } from '../layouts/adminNav';
import StatCard from '../components/admin/StatCard';
import ConfirmDialog from '../components/admin/ConfirmDialog';
import ChartCard from '../charts/ChartCard';
import MonthlyRevenueChart from '../charts/MonthlyRevenueChart';
import AdminDashboardHomePage from '../pages/admin/AdminDashboardHomePage';
import AdminRoomsPage from '../pages/admin/AdminRoomsPage';
import AdminGuestsPage from '../pages/admin/AdminGuestsPage';
import AdminBookingsPage from '../pages/admin/AdminBookingsPage';
import AdminPaymentsPage from '../pages/admin/AdminPaymentsPage';
import AdminReportsPage from '../pages/admin/AdminReportsPage';
import { AdminUiProvider } from '../context/AdminUiContext';
import { occupancyPercent, toChartRows } from '../utils/adminDates';

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    isAuthenticated: true,
    user: { firstName: 'Ada', lastName: 'Admin', email: 'admin@example.com' },
    roles: ['ADMIN'],
    logout: vi.fn(),
  }),
}));

vi.mock('../services/adminReportService', () => ({
  adminReportService: {
    getDashboard: vi.fn().mockResolvedValue({
      totalGuests: 40,
      totalRooms: 20,
      availableRooms: 12,
      occupiedRooms: 8,
      todaysBookings: 3,
      todaysRevenue: 15000,
      monthlyRevenue: 200000,
      pendingPayments: 2,
      recentBookings: [
        {
          id: 1,
          guestName: 'Ada Lovelace',
          checkInDate: '2026-08-10',
          status: 'CONFIRMED',
          totalAmount: 10000,
        },
      ],
    }),
    getRevenue: vi.fn().mockResolvedValue({
      totalRevenue: 200000,
      series: [{ label: '2026-08-01', amount: 5000, count: 1 }],
      byRoomType: [{ label: 'DELUXE', amount: 80000, count: 10 }],
    }),
    getOccupancy: vi.fn().mockResolvedValue({
      periodOccupancyPercent: 72.5,
      dailyOccupancy: [{ label: '2026-08-01', amount: 70, count: 1 }],
    }),
    getBookings: vi.fn().mockResolvedValue({
      totalBookings: 55,
      byStatus: [{ label: 'CONFIRMED', count: 20, amount: 20 }],
      byCheckInDate: [{ label: '2026-08-10', count: 4, amount: 4 }],
    }),
    getPayments: vi.fn().mockResolvedValue({
      byStatus: [{ label: 'SUCCESS', count: 30, amount: 30 }],
      totalCollected: 180000,
      paymentCount: 30,
    }),
    getMonthly: vi.fn().mockResolvedValue({
      year: 2026,
      month: 8,
      monthlyGuestRegistrations: 12,
      monthlyRevenue: 200000,
      monthlyBookings: 40,
    }),
  },
}));

vi.mock('../services/roomService', () => ({
  roomService: {
    search: vi.fn().mockResolvedValue({
      content: [
        {
          id: 1,
          roomNumber: '101',
          roomType: 'DELUXE',
          status: 'AVAILABLE',
          capacity: 2,
          pricePerNight: 5000,
        },
      ],
      totalElements: 1,
      totalPages: 1,
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

vi.mock('../services/guestService', () => ({
  guestService: {
    list: vi.fn().mockResolvedValue({
      content: [
        { id: 7, firstName: 'Ada', lastName: 'Lovelace', email: 'ada@example.com', phone: '+91 1' },
      ],
      totalElements: 1,
      number: 0,
      size: 10,
    }),
    searchByName: vi.fn(),
    searchByEmail: vi.fn(),
    searchByPhone: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    remove: vi.fn(),
  },
}));

vi.mock('../services/bookingService', () => ({
  bookingService: {
    list: vi.fn().mockResolvedValue({
      content: [
        {
          id: 99,
          guestFirstName: 'Ada',
          guestLastName: 'Lovelace',
          checkInDate: '2026-08-10',
          checkOutDate: '2026-08-12',
          status: 'CONFIRMED',
          totalAmount: 10000,
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
    listByGuest: vi.fn().mockResolvedValue({ content: [] }),
  },
}));

vi.mock('../services/paymentService', () => ({
  paymentService: {
    history: vi.fn().mockResolvedValue({
      content: [
        {
          id: 5,
          bookingId: 99,
          status: 'SUCCESS',
          amount: 10000,
          createdAt: '2026-08-01T10:00:00',
        },
      ],
      totalElements: 1,
      number: 0,
      size: 10,
    }),
    downloadInvoicePdf: vi.fn(),
    refund: vi.fn(),
  },
}));

const theme = createTheme();

function renderAdmin(ui, path = '/admin/dashboard') {
  return render(
    <ThemeProvider theme={theme}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/admin" element={<AdminLayout />}>
            <Route path="dashboard" element={ui} />
            <Route path="rooms" element={ui} />
            <Route path="guests" element={ui} />
            <Route path="bookings" element={ui} />
            <Route path="payments" element={ui} />
            <Route path="reports" element={ui} />
          </Route>
        </Routes>
      </MemoryRouter>
    </ThemeProvider>
  );
}

describe('admin utils', () => {
  it('computes occupancy percent', () => {
    expect(occupancyPercent({ totalRooms: 20, occupiedRooms: 8 })).toBe(40);
  });

  it('maps labeled amounts to chart rows', () => {
    expect(toChartRows([{ label: 'A', amount: 10, count: 2 }])[0]).toMatchObject({
      name: 'A',
      amount: 10,
      count: 2,
    });
  });
});

describe('navigation & layout', () => {
  it('renders sidebar nav items and top bar', () => {
    renderAdmin(<div>child</div>);
    expect(screen.getByTestId('admin-sidebar')).toBeInTheDocument();
    expect(screen.getByTestId('admin-topbar')).toBeInTheDocument();
    ADMIN_NAV.forEach((item) => {
      expect(screen.getByText(item.label)).toBeInTheDocument();
    });
  });

  it('opens temporary drawer affordance via matchMedia mobile path without crash', () => {
    window.matchMedia = vi.fn().mockImplementation((query) => ({
      matches: String(query).includes('max-width'),
      media: query,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      addListener: vi.fn(),
      removeListener: vi.fn(),
      dispatchEvent: vi.fn(),
    }));
    renderAdmin(<div>ok</div>);
    expect(screen.getByLabelText(/open menu/i)).toBeInTheDocument();
  });
});

describe('shared components', () => {
  it('renders stat card', () => {
    render(
      <ThemeProvider theme={theme}>
        <StatCard title="Revenue" value="₹1" subtitle="today" />
      </ThemeProvider>
    );
    expect(screen.getByTestId('stat-card')).toHaveTextContent('Revenue');
  });

  it('confirm dialog fires confirm', () => {
    const onConfirm = vi.fn();
    render(
      <ThemeProvider theme={theme}>
        <ConfirmDialog open title="Delete?" message="Sure?" onConfirm={onConfirm} onClose={vi.fn()} />
      </ThemeProvider>
    );
    fireEvent.click(screen.getByRole('button', { name: /confirm/i }));
    expect(onConfirm).toHaveBeenCalled();
  });

  it('chart card shows title', () => {
    render(
      <ThemeProvider theme={theme}>
        <ChartCard title="Demo"><div>chart</div></ChartCard>
      </ThemeProvider>
    );
    expect(screen.getByTestId('chart-card')).toHaveTextContent('Demo');
  });

  it('monthly revenue chart renders with data', () => {
    const { container } = render(
      <ThemeProvider theme={theme}>
        <MonthlyRevenueChart series={[{ label: 'D1', amount: 100 }]} />
      </ThemeProvider>
    );
    expect(container.querySelector('.recharts-responsive-container')).toBeTruthy();
  });
});

describe('AdminDashboardHomePage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('shows KPI cards and charts', async () => {
    renderAdmin(<AdminDashboardHomePage />);
    expect(await screen.findByTestId('admin-dashboard-home')).toBeInTheDocument();
    expect(await screen.findByText('Monthly revenue')).toBeInTheDocument();
    expect(screen.getByTestId('quick-actions')).toBeInTheDocument();
    expect(screen.getAllByTestId('chart-card').length).toBeGreaterThan(3);
  });
});

describe('management pages', () => {
  beforeEach(() => vi.clearAllMocks());

  it('rooms page lists rooms and opens create', async () => {
    renderAdmin(<AdminRoomsPage />, '/admin/rooms');
    expect(await screen.findByTestId('admin-rooms-page')).toBeInTheDocument();
    expect(await screen.findByText('101')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /create room/i }));
    expect(await screen.findByRole('dialog')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /create room/i })).toBeInTheDocument();
  });

  it('guests page lists guests', async () => {
    renderAdmin(<AdminGuestsPage />, '/admin/guests');
    expect(await screen.findByTestId('admin-guests-page')).toBeInTheDocument();
    expect(await screen.findByText(/ada@example.com/i)).toBeInTheDocument();
  });

  it('bookings page lists bookings', async () => {
    renderAdmin(<AdminBookingsPage />, '/admin/bookings');
    expect(await screen.findByTestId('admin-bookings-page')).toBeInTheDocument();
    expect(await screen.findByText('99')).toBeInTheDocument();
  });

  it('payments page lists payments', async () => {
    renderAdmin(<AdminPaymentsPage />, '/admin/payments');
    expect(await screen.findByTestId('admin-payments-page')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('SUCCESS')).toBeInTheDocument());
  });

  it('reports page tabs render', async () => {
    renderAdmin(<AdminReportsPage />, '/admin/reports');
    expect(await screen.findByTestId('admin-reports-page')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('tab', { name: /occupancy/i }));
    expect(await screen.findByText(/occupancy report/i)).toBeInTheDocument();
  });
});

describe('AdminUi snackbar', () => {
  it('provider mounts children', () => {
    render(
      <ThemeProvider theme={theme}>
        <AdminUiProvider>
          <div data-testid="child">x</div>
        </AdminUiProvider>
      </ThemeProvider>
    );
    expect(screen.getByTestId('child')).toBeInTheDocument();
  });
});
