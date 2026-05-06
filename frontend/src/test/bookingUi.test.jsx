import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material';
import { BookingWizardProvider } from '../context/BookingWizardContext';
import BookingFormStep from '../components/booking/BookingFormStep';
import GuestFormStep from '../components/booking/GuestFormStep';
import BookingSummaryStep from '../components/booking/BookingSummaryStep';
import PriceBreakdown from '../components/booking/PriceBreakdown';
import BookingSuccessPage from '../pages/booking/BookingSuccessPage';
import BookingFailurePage from '../pages/booking/BookingFailurePage';
import BookingPaymentPage from '../pages/booking/BookingPaymentPage';
import BookingStepper, { BOOKING_STEPS } from '../components/booking/BookingStepper';
import { calculateBookingPrice } from '../utils/priceCalculation';
import { validateBookingForm, validateGuestForm } from '../utils/bookingValidation';
import { WIZARD_DRAFT_KEY, loadWizardDraft, saveWizardDraft, clearWizardDraft } from '../utils/wizardDraftStorage';
import { guestSchema } from '../utils/bookingSchemas';

vi.mock('../hooks/useAuth', () => ({
  useAuth: vi.fn(() => ({
    isAuthenticated: true,
    user: {
      email: 'guest@example.com',
      firstName: 'Ada',
      lastName: 'Lovelace',
    },
  })),
}));

vi.mock('../services/bookingService', () => ({
  bookingService: {
    checkAvailability: vi.fn().mockResolvedValue({
      rooms: [{ roomId: 1, available: true }],
    }),
    create: vi.fn().mockResolvedValue({
      id: 99,
      totalAmount: 12000,
      checkInDate: '2026-08-10',
      checkOutDate: '2026-08-12',
      numberOfNights: 2,
      guestEmail: 'guest@example.com',
      guestFirstName: 'Ada',
      guestLastName: 'Lovelace',
      status: 'PENDING',
      rooms: [{ roomId: 1, roomNumber: '101', pricePerNight: 5000 }],
    }),
    getById: vi.fn(),
  },
}));

vi.mock('../services/guestService', () => ({
  guestService: {
    upsertFromForm: vi.fn().mockResolvedValue({ id: 7, email: 'guest@example.com' }),
  },
}));

vi.mock('../services/paymentService', () => ({
  paymentService: {
    createOrder: vi.fn().mockResolvedValue({
      paymentId: 1,
      bookingId: 99,
      razorpayOrderId: 'order_mock_1001',
      amount: 12000,
      status: 'CREATED',
    }),
    verify: vi.fn().mockResolvedValue({ id: 1, status: 'SUCCESS' }),
    downloadInvoicePdf: vi.fn().mockResolvedValue(undefined),
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
          capacity: 3,
          pricePerNight: 6000,
          effectivePrice: 5000,
        },
        {
          id: 2,
          roomNumber: '102',
          roomType: 'STANDARD',
          capacity: 2,
          pricePerNight: 3000,
          effectivePrice: 3000,
        },
      ],
    }),
  },
}));

vi.mock('../utils/mockPaymentSign', () => ({
  isMockOrder: () => true,
  signMockPayment: vi.fn().mockResolvedValue('signed'),
}));

import { bookingService } from '../services/bookingService';
import { guestService } from '../services/guestService';
import { paymentService } from '../services/paymentService';

const theme = createTheme();

const sampleRoom = {
  id: 1,
  roomNumber: '101',
  roomType: 'DELUXE',
  capacity: 3,
  pricePerNight: 6000,
  effectivePrice: 5000,
};

function renderWithWizard(ui, wizardProps = {}) {
  return render(
    <ThemeProvider theme={theme}>
      <MemoryRouter>
        <BookingWizardProvider
          initial={{
            roomId: 1,
            roomIds: [1],
            room: sampleRoom,
            rooms: [sampleRoom],
            checkIn: '2026-08-10',
            checkOut: '2026-08-12',
            guests: 2,
            availabilityOk: true,
            guest: {
              firstName: 'Ada',
              lastName: 'Lovelace',
              email: 'guest@example.com',
              phone: '+91 9876543210',
              address: 'Bengaluru',
            },
            ...wizardProps,
          }}
        >
          {ui}
        </BookingWizardProvider>
      </MemoryRouter>
    </ThemeProvider>
  );
}

describe('priceCalculation & validation', () => {
  beforeEach(() => {
    clearWizardDraft();
  });

  it('calculates room charges, taxes, service, discount, grand total', () => {
    const price = calculateBookingPrice({
      pricePerNight: 6000,
      effectivePrice: 5000,
      checkIn: '2026-08-10',
      checkOut: '2026-08-12',
    });
    expect(price.nights).toBe(2);
    expect(price.roomCharges).toBe(10000);
    expect(price.discount).toBe(2000);
    expect(price.taxes).toBe(1200);
    expect(price.serviceCharges).toBe(500);
    expect(price.grandTotal).toBe(11700);
  });

  it('calculates multi-room lines', () => {
    const price = calculateBookingPrice({
      rooms: [
        sampleRoom,
        { id: 2, roomNumber: '102', roomType: 'STANDARD', pricePerNight: 3000, effectivePrice: 3000 },
      ],
      checkIn: '2026-08-10',
      checkOut: '2026-08-12',
    });
    expect(price.roomCount).toBe(2);
    expect(price.lines).toHaveLength(2);
    expect(price.roomCharges).toBe(16000);
  });

  it('validates booking dates and guest capacity', () => {
    const errors = validateBookingForm({
      checkIn: '2020-01-01',
      checkOut: '2020-01-01',
      guests: 5,
      roomId: 1,
      capacity: 2,
    });
    expect(errors.checkIn).toBeTruthy();
    expect(errors.checkOut).toBeTruthy();
    expect(errors.guests).toMatch(/sleeps up to 2/i);
  });

  it('validates guest email and phone', () => {
    const errors = validateGuestForm({
      firstName: 'A',
      lastName: 'B',
      email: 'bad',
      phone: '12',
    });
    expect(errors.email).toBeTruthy();
    expect(errors.phone).toBeTruthy();
  });

  it('zod guest schema rejects bad phone', () => {
    const result = guestSchema.safeParse({
      firstName: 'Ada',
      lastName: 'Lovelace',
      email: 'a@b.com',
      phone: 'bad',
      address: '',
    });
    expect(result.success).toBe(false);
  });
});

describe('wizard draft sessionStorage', () => {
  beforeEach(() => clearWizardDraft());

  it('persists and loads draft', () => {
    saveWizardDraft({
      step: 1,
      roomIds: [1, 2],
      checkIn: '2026-08-10',
      checkOut: '2026-08-12',
      guests: 2,
      specialRequests: 'Late',
      guest: { firstName: 'Ada', lastName: 'L', email: 'a@b.com', phone: '+91 1', address: '' },
    });
    expect(sessionStorage.getItem(WIZARD_DRAFT_KEY)).toBeTruthy();
    const loaded = loadWizardDraft();
    expect(loaded.step).toBe(1);
    expect(loaded.roomIds).toEqual([1, 2]);
    clearWizardDraft();
    expect(loadWizardDraft()).toBeNull();
  });
});

describe('BookingFormStep', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    clearWizardDraft();
  });

  it('shows nights live label and continues when valid', async () => {
    const onNext = vi.fn();
    renderWithWizard(<BookingFormStep onNext={onNext} />);

    expect(await screen.findByTestId('nights-live-label')).toHaveTextContent(/2 night/i);
    expect(screen.getByLabelText(/check-in date/i)).toBeInTheDocument();

    fireEvent.click(screen.getByTestId('continue-guest'));
    await waitFor(() => expect(onNext).toHaveBeenCalled());
  });

  it('blocks continue when availability is false', async () => {
    bookingService.checkAvailability.mockResolvedValue({
      rooms: [{ roomId: 1, available: false, reason: 'Already booked' }],
    });
    const onNext = vi.fn();
    renderWithWizard(<BookingFormStep onNext={onNext} />, { availabilityOk: false });

    await waitFor(() => {
      expect(screen.getByTestId('continue-guest')).toBeDisabled();
    });
    expect(await screen.findByTestId('availability-blocked')).toBeInTheDocument();
    fireEvent.click(screen.getByTestId('continue-guest'));
    expect(onNext).not.toHaveBeenCalled();
  });
});

describe('GuestFormStep', () => {
  beforeEach(() => clearWizardDraft());

  it('auto-fills logged-in user and validates phone', async () => {
    const onNext = vi.fn();
    renderWithWizard(
      <GuestFormStep onNext={onNext} onBack={vi.fn()} />,
      {
        guest: {
          firstName: '',
          lastName: '',
          email: '',
          phone: '',
          address: '',
        },
      }
    );

    expect(await screen.findByDisplayValue('guest@example.com')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Ada')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText(/phone number/i), {
      target: { value: 'bad' },
    });
    fireEvent.click(screen.getByRole('button', { name: /continue to summary/i }));

    expect(await screen.findByText(/valid phone/i)).toBeInTheDocument();
    expect(onNext).not.toHaveBeenCalled();
  });
});

describe('BookingSummaryStep', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    clearWizardDraft();
  });

  it('shows price breakdown, edit guest, and creates booking with roomIds', async () => {
    const onCreated = vi.fn();
    const onEditGuest = vi.fn();
    renderWithWizard(
      <BookingSummaryStep onBack={vi.fn()} onCreated={onCreated} onEditGuest={onEditGuest} />
    );

    expect(screen.getByTestId('price-breakdown')).toBeInTheDocument();
    expect(screen.getByTestId('tax-rate-tooltip')).toBeInTheDocument();
    fireEvent.click(screen.getByTestId('edit-guest'));
    expect(onEditGuest).toHaveBeenCalled();

    fireEvent.click(screen.getByTestId('confirm-booking'));

    await waitFor(() => {
      expect(guestService.upsertFromForm).toHaveBeenCalled();
      expect(bookingService.create).toHaveBeenCalledWith(
        expect.objectContaining({ roomIds: [1] })
      );
      expect(onCreated).toHaveBeenCalledWith(expect.objectContaining({ id: 99 }));
    });
  });

  it('lists two rooms in summary', () => {
    const room2 = {
      id: 2,
      roomNumber: '102',
      roomType: 'STANDARD',
      capacity: 2,
      pricePerNight: 3000,
      effectivePrice: 3000,
    };
    renderWithWizard(<BookingSummaryStep onBack={vi.fn()} onCreated={vi.fn()} onEditGuest={vi.fn()} />, {
      roomIds: [1, 2],
      rooms: [sampleRoom, room2],
    });
    expect(screen.getByText(/DELUXE 101 ·/i)).toBeInTheDocument();
    expect(screen.getByText(/STANDARD 102 ·/i)).toBeInTheDocument();
  });
});

describe('PriceBreakdown & Stepper', () => {
  it('renders breakdown rows and tax tooltip', () => {
    const price = calculateBookingPrice({
      pricePerNight: 5000,
      effectivePrice: 5000,
      checkIn: '2026-08-10',
      checkOut: '2026-08-11',
    });
    render(
      <ThemeProvider theme={theme}>
        <PriceBreakdown price={price} />
      </ThemeProvider>
    );
    expect(screen.getByText(/room charges/i)).toBeInTheDocument();
    expect(screen.getByTestId('tax-rate-tooltip')).toBeInTheDocument();
  });

  it('shows all booking progress steps', () => {
    render(
      <ThemeProvider theme={theme}>
        <BookingStepper activeStep={1} />
      </ThemeProvider>
    );
    BOOKING_STEPS.forEach((label) => {
      expect(screen.getByText(label)).toBeInTheDocument();
    });
  });
});

describe('BookingSuccessPage', () => {
  it('shows booking id, guest, payment status, invoice button', () => {
    render(
      <ThemeProvider theme={theme}>
        <MemoryRouter
          initialEntries={[
            {
              pathname: '/book/success/99',
              state: {
                booking: {
                  id: 99,
                  guestFirstName: 'Ada',
                  guestLastName: 'Lovelace',
                  guestEmail: 'guest@example.com',
                  checkInDate: '2026-08-10',
                  checkOutDate: '2026-08-12',
                  numberOfNights: 2,
                  totalAmount: 12000,
                  status: 'CONFIRMED',
                  rooms: [{ roomNumber: '101', pricePerNight: 5000 }],
                },
                payment: { status: 'SUCCESS' },
              },
            },
          ]}
        >
          <Routes>
            <Route path="/book/success/:bookingId" element={<BookingSuccessPage />} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>
    );

    const page = screen.getByTestId('booking-success');
    expect(within(page).getByText(/#99/)).toBeInTheDocument();
    expect(within(page).getByText(/Ada Lovelace/)).toBeInTheDocument();
    expect(within(page).getByText(/SUCCESS/)).toBeInTheDocument();
    expect(screen.getByTestId('download-invoice')).toBeInTheDocument();
  });

  it('downloads invoice on click', async () => {
    render(
      <ThemeProvider theme={theme}>
        <MemoryRouter
          initialEntries={[
            {
              pathname: '/book/success/99',
              state: {
                booking: {
                  id: 99,
                  guestEmail: 'a@b.com',
                  totalAmount: 1,
                  rooms: [],
                },
                payment: { status: 'SUCCESS' },
              },
            },
          ]}
        >
          <Routes>
            <Route path="/book/success/:bookingId" element={<BookingSuccessPage />} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>
    );

    fireEvent.click(screen.getByTestId('download-invoice'));
    await waitFor(() => {
      expect(paymentService.downloadInvoicePdf).toHaveBeenCalledWith('99');
    });
  });
});

describe('BookingFailurePage', () => {
  it('offers retry payment and support', () => {
    render(
      <ThemeProvider theme={theme}>
        <MemoryRouter initialEntries={['/book/failure/99']}>
          <Routes>
            <Route path="/book/failure/:bookingId" element={<BookingFailurePage />} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>
    );

    expect(screen.getByTestId('booking-failure')).toBeInTheDocument();
    expect(screen.getByTestId('retry-payment')).toHaveAttribute('href', '/book/payment/99');
    expect(screen.getByRole('link', { name: /contact support/i })).toBeInTheDocument();
  });
});

describe('BookingPaymentPage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('loads order and pays successfully', async () => {
    render(
      <ThemeProvider theme={theme}>
        <MemoryRouter
          initialEntries={[
            {
              pathname: '/book/payment/99',
              state: {
                booking: {
                  id: 99,
                  totalAmount: 12000,
                  numberOfNights: 2,
                  checkInDate: '2026-08-10',
                  checkOutDate: '2026-08-12',
                  rooms: [{ roomId: 1 }],
                },
              },
            },
          ]}
        >
          <Routes>
            <Route path="/book/payment/:bookingId" element={<BookingPaymentPage />} />
            <Route path="/book/success/:bookingId" element={<div>OK</div>} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>
    );

    expect(await screen.findByTestId('pay-now')).toBeInTheDocument();
    expect(screen.getByTestId('order-summary')).toBeInTheDocument();
    fireEvent.click(screen.getByTestId('pay-now'));

    await waitFor(() => {
      expect(paymentService.verify).toHaveBeenCalled();
      expect(screen.getByText('OK')).toBeInTheDocument();
    });
  });
});

describe('responsive booking shell', () => {
  it('booking layout stepper stacks labels without crashing at narrow width', () => {
    window.matchMedia = vi.fn().mockImplementation((query) => ({
      matches: query.includes('max-width'),
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    }));

    render(
      <ThemeProvider theme={theme}>
        <BookingStepper activeStep={0} />
      </ThemeProvider>
    );
    expect(screen.getByText('Select Room')).toBeInTheDocument();
  });
});
