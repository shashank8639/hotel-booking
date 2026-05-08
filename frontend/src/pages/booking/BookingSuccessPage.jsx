import { useEffect, useState } from 'react';
import { Link as RouterLink, useLocation, useParams } from 'react-router-dom';
import {
  Alert,
  Button,
  CircularProgress,
  Divider,
  Stack,
  Typography,
} from '@mui/material';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import BookingLayout from '../../layouts/BookingLayout';
import BookingLoadingScreen from '../../components/booking/BookingLoadingScreen';
import { bookingService } from '../../services/bookingService';
import { paymentService } from '../../services/paymentService';
import { formatCurrency, formatDateLabel } from '../../utils/format';

/**
 * Step 5 — confirmation: booking id, guest/room/payment, invoice download.
 */
export default function BookingSuccessPage() {
  const { bookingId } = useParams();
  const location = useLocation();
  const [booking, setBooking] = useState(location.state?.booking || null);
  const [payment, setPayment] = useState(location.state?.payment || null);
  const [loading, setLoading] = useState(!location.state?.booking);
  const [downloadError, setDownloadError] = useState('');
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      if (location.state?.booking) return;
      setLoading(true);
      try {
        const data = await bookingService.getById(bookingId);
        if (!cancelled) setBooking(data);
      } catch {
        if (!cancelled) setBooking(null);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [bookingId, location.state]);

  const handleInvoice = async () => {
    setDownloadError('');
    setDownloading(true);
    try {
      await paymentService.downloadInvoicePdf(bookingId);
    } catch (err) {
      setDownloadError(err.message || 'Invoice download failed');
    } finally {
      setDownloading(false);
    }
  };

  if (loading) {
    return (
      <BookingLayout activeStep={4}>
        <BookingLoadingScreen message="Loading confirmation…" />
      </BookingLayout>
    );
  }

  if (!booking) {
    return (
      <BookingLayout activeStep={4}>
        <Alert severity="warning">Booking not found.</Alert>
        <Button component={RouterLink} to="/rooms" sx={{ mt: 2 }}>
          Browse rooms
        </Button>
      </BookingLayout>
    );
  }

  const roomLabels =
    booking.rooms
      ?.map((r) => `Room ${r.roomNumber || r.roomId}${r.pricePerNight != null ? ` · ${formatCurrency(r.pricePerNight)}/night` : ''}`)
      .join(', ') || '—';

  return (
    <BookingLayout activeStep={4} maxWidth="sm">
      <Stack spacing={2} alignItems="flex-start" data-testid="booking-success">
        <CheckCircleOutlineIcon color="success" sx={{ fontSize: 56 }} />
        <Typography variant="h4" fontWeight={750}>
          Booking confirmed
        </Typography>
        <Typography color="text.secondary">
          A confirmation email will be sent to {booking.guestEmail} (when email notifications are
          enabled).
        </Typography>

        <Divider flexItem />

        <Detail label="Booking ID" value={`#${booking.id}`} />
        <Detail
          label="Guest"
          value={`${booking.guestFirstName || ''} ${booking.guestLastName || ''}`.trim()}
        />
        <Detail label="Email" value={booking.guestEmail} />
        <Detail
          label="Hotel"
          value={booking.hotelName || booking.rooms?.[0]?.hotelName || 'StayFinder'}
        />
        <Detail label="Room(s)" value={roomLabels} />
        <Detail label="Check-in" value={formatDateLabel(booking.checkInDate)} />
        <Detail label="Check-out" value={formatDateLabel(booking.checkOutDate)} />
        <Detail label="Nights" value={String(booking.numberOfNights ?? '—')} />
        <Detail label="Total paid" value={formatCurrency(booking.totalAmount)} />
        <Detail
          label="Payment status"
          value={payment?.status || booking.status || 'SUCCESS'}
        />

        {downloadError && <Alert severity="error">{downloadError}</Alert>}

        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ pt: 1 }}>
          <Button
            variant="contained"
            onClick={handleInvoice}
            disabled={downloading}
            data-testid="download-invoice"
          >
            {downloading ? <CircularProgress size={22} color="inherit" /> : 'Download invoice'}
          </Button>
          <Button component={RouterLink} to="/customer/dashboard" variant="outlined">
            My dashboard
          </Button>
          <Button component={RouterLink} to="/rooms" variant="text">
            Book another stay
          </Button>
        </Stack>
      </Stack>
    </BookingLayout>
  );
}

function Detail({ label, value }) {
  return (
    <Stack direction="row" justifyContent="space-between" spacing={2} sx={{ width: '100%' }}>
      <Typography color="text.secondary">{label}</Typography>
      <Typography fontWeight={600}>{value || '—'}</Typography>
    </Stack>
  );
}
