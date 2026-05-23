import { useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import BookingLoadingScreen from '../components/booking/BookingLoadingScreen';

/**
 * Legacy Module 11 checkout URL — redirects into Module 12 payment page.
 */
export default function CheckoutPage() {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    navigate(`/book/payment/${bookingId}`, {
      replace: true,
      state: location.state,
    });
  }, [bookingId, navigate, location.state]);

  return <BookingLoadingScreen message="Redirecting to payment…" />;
}
