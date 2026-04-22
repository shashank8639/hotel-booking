import { useNavigate, useLocation, useParams } from 'react-router-dom';
import { Alert, Button, Container } from '@mui/material';
import BookingLayout from '../../layouts/BookingLayout';
import BookingLoadingScreen from '../../components/booking/BookingLoadingScreen';
import PaymentPanel from '../../components/booking/PaymentPanel';
import { usePaymentCheckout } from '../../hooks/usePaymentCheckout';
import { calculateBookingPrice } from '../../utils/priceCalculation';

/**
 * Step 4 — create payment order + verify (mock Razorpay) → success / failure.
 */
export default function BookingPaymentPage() {
  const { bookingId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  const {
    booking,
    order,
    loading,
    paying,
    progress,
    error,
    setError,
    pay,
  } = usePaymentCheckout(bookingId, location.state?.booking);

  const estimate =
    booking &&
    calculateBookingPrice({
      pricePerNight: booking.totalAmount / Math.max(booking.numberOfNights || 1, 1),
      effectivePrice: booking.totalAmount / Math.max(booking.numberOfNights || 1, 1),
      checkIn: booking.checkInDate,
      checkOut: booking.checkOutDate,
    });

  const handlePay = async () => {
    try {
      const result = await pay();
      navigate(`/book/success/${bookingId}`, {
        replace: true,
        state: { booking, payment: result, order },
      });
    } catch (err) {
      navigate(`/book/failure/${bookingId}`, {
        replace: false,
        state: {
          booking,
          order,
          message: err.message || error || 'Payment could not be completed',
        },
      });
    }
  };

  if (loading) {
    return (
      <BookingLayout activeStep={3}>
        <BookingLoadingScreen message="Preparing payment order…" showLinear />
      </BookingLayout>
    );
  }

  if (!booking && error) {
    return (
      <Container sx={{ py: 4 }}>
        <Alert severity="error">{error}</Alert>
        <Button sx={{ mt: 2 }} onClick={() => navigate('/rooms')}>
          Back to rooms
        </Button>
      </Container>
    );
  }

  return (
    <BookingLayout activeStep={3} maxWidth="sm">
      <PaymentPanel
        booking={booking}
        order={order}
        estimate={estimate}
        paying={paying}
        progress={progress}
        error={error}
        onPay={handlePay}
        onRetry={() => {
          setError('');
          handlePay();
        }}
        onReturn={() => navigate(`/book?roomId=${booking?.rooms?.[0]?.roomId || ''}`)}
      />
    </BookingLayout>
  );
}
