import { Link as RouterLink, useLocation, useParams } from 'react-router-dom';
import { Alert, Button, Stack, Typography } from '@mui/material';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import BookingLayout from '../../layouts/BookingLayout';

/**
 * Payment failure — retry, return to booking, or contact support.
 */
export default function BookingFailurePage() {
  const { bookingId } = useParams();
  const location = useLocation();
  const message =
    location.state?.message ||
    'We could not complete your payment. Your booking may still be pending payment.';

  return (
    <BookingLayout activeStep={3} maxWidth="sm">
      <Stack spacing={2} alignItems="flex-start" data-testid="booking-failure">
        <ErrorOutlineIcon color="error" sx={{ fontSize: 56 }} />
        <Typography variant="h4" fontWeight={750}>
          Payment failed
        </Typography>
        <Alert severity="error" sx={{ width: '100%' }}>
          {message}
        </Alert>
        <Typography color="text.secondary">
          Booking reference: #{bookingId}. You can retry payment without creating a new booking.
        </Typography>

        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ pt: 1 }}>
          <Button
            component={RouterLink}
            to={`/book/payment/${bookingId}`}
            state={location.state}
            variant="contained"
            data-testid="retry-payment"
          >
            Retry payment
          </Button>
          <Button component={RouterLink} to="/rooms" variant="outlined">
            Return to booking search
          </Button>
          <Button
            component="a"
            href="mailto:support@grandhorizon.example?subject=Payment%20help%20booking%20"
            variant="text"
          >
            Contact support
          </Button>
        </Stack>
      </Stack>
    </BookingLayout>
  );
}
