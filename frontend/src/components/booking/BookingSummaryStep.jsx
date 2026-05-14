import { useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Divider,
  Link,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import { useBookingWizard } from '../../hooks/useBookingWizard';
import { useBookingPrice } from '../../hooks/useBookingPrice';
import { bookingService } from '../../services/bookingService';
import { guestService } from '../../services/guestService';
import { clearWizardDraft } from '../../utils/wizardDraftStorage';
import { formatCurrency, formatDateLabel } from '../../utils/format';
import PriceBreakdown from './PriceBreakdown';

/**
 * Step 3 — review booking + create booking via API, then navigate to payment.
 */
export default function BookingSummaryStep({ onBack, onCreated, onEditGuest }) {
  const {
    room,
    rooms,
    roomIds,
    roomId,
    checkIn,
    checkOut,
    guests,
    specialRequests,
    guest,
    setGuestRecord,
    setBooking,
  } = useBookingWizard();

  const selectedRooms = rooms?.length ? rooms : room ? [room] : [];
  const price = useBookingPrice({ rooms: selectedRooms, checkIn, checkOut });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const buildSpecialRequests = () => {
    const parts = [];
    if (guest.address?.trim()) {
      parts.push(`Address: ${guest.address.trim()}`);
    }
    if (specialRequests?.trim()) {
      parts.push(specialRequests.trim());
    }
    parts.push(`Party size: ${guests}`);
    return parts.join('\n') || undefined;
  };

  const handleConfirm = async () => {
    setError('');
    setSubmitting(true);
    try {
      const guestRecord = await guestService.upsertFromForm(guest);
      setGuestRecord(guestRecord);

      const ids = (roomIds?.length ? roomIds : [roomId]).filter(Boolean).map(Number);
      const booking = await bookingService.create({
        guestId: guestRecord.id,
        checkInDate: checkIn,
        checkOutDate: checkOut,
        roomIds: ids,
        specialRequests: buildSpecialRequests(),
      });
      setBooking(booking);
      clearWizardDraft();
      onCreated(booking);
    } catch (err) {
      setError(err.message || 'Could not create booking');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        Booking summary
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        Review details before we create your booking and open payment.
      </Typography>

      <Stack spacing={2}>
        <Paper variant="outlined" sx={{ p: 2 }}>
          <Typography fontWeight={700} gutterBottom>
            Stay
          </Typography>
          {selectedRooms.map((r) => (
            <Detail
              key={r.id}
              label="Room"
              value={`${r.roomType} ${r.roomNumber} · ${formatCurrency(r.effectivePrice ?? r.pricePerNight)}/night`}
            />
          ))}
          <Detail label="Check-in" value={formatDateLabel(checkIn)} />
          <Detail label="Check-out" value={formatDateLabel(checkOut)} />
          <Detail label="Nights" value={String(price.nights)} />
          <Detail label="Guests" value={String(guests)} />
          {specialRequests && <Detail label="Requests" value={specialRequests} />}
        </Paper>

        <Paper variant="outlined" sx={{ p: 2 }}>
          <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
            <Typography fontWeight={700}>Guest</Typography>
            <Link
              component="button"
              type="button"
              variant="body2"
              onClick={onEditGuest}
              data-testid="edit-guest"
              underline="hover"
            >
              Edit guest
            </Link>
          </Stack>
          <Detail label="Name" value={`${guest.firstName} ${guest.lastName}`} />
          <Detail label="Email" value={guest.email} />
          <Detail label="Phone" value={guest.phone} />
          {guest.address && <Detail label="Address" value={guest.address} />}
        </Paper>

        <Paper variant="outlined" sx={{ p: 2 }}>
          <Typography fontWeight={700} gutterBottom>
            Price calculation
          </Typography>
          <PriceBreakdown price={price} />
        </Paper>

        {error && <Alert severity="error">{error}</Alert>}

        <Divider />

        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
          <Button variant="outlined" onClick={onBack} disabled={submitting}>
            Back
          </Button>
          <Button
            variant="contained"
            size="large"
            onClick={handleConfirm}
            disabled={submitting}
            data-testid="confirm-booking"
          >
            {submitting ? (
              <CircularProgress size={24} color="inherit" />
            ) : (
              `Confirm & pay ${formatCurrency(price.grandTotal)}`
            )}
          </Button>
        </Stack>
      </Stack>
    </Box>
  );
}

function Detail({ label, value }) {
  return (
    <Stack direction="row" justifyContent="space-between" spacing={2} sx={{ py: 0.5 }}>
      <Typography color="text.secondary">{label}</Typography>
      <Typography fontWeight={600} textAlign="right">
        {value || '—'}
      </Typography>
    </Stack>
  );
}
