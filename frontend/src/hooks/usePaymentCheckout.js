import { useCallback, useEffect, useState } from 'react';
import { bookingService } from '../services/bookingService';
import { paymentService } from '../services/paymentService';
import { isMockOrder, signMockPayment } from '../utils/mockPaymentSign';

/**
 * Loads booking + payment order, then verifies payment (mock or failure path).
 */
export function usePaymentCheckout(bookingId, initialBooking) {
  const [booking, setBooking] = useState(initialBooking || null);
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState('');
  const [payment, setPayment] = useState(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      setError('');
      try {
        const current =
          initialBooking?.id === Number(bookingId)
            ? initialBooking
            : await bookingService.getById(bookingId);
        if (cancelled) return;
        setBooking(current);
        const paymentOrder = await paymentService.createOrder(Number(bookingId));
        if (!cancelled) setOrder(paymentOrder);
      } catch (err) {
        if (!cancelled) setError(err.message || 'Could not prepare payment');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    if (bookingId) load();
    return () => {
      cancelled = true;
    };
  }, [bookingId, initialBooking]);

  const pay = useCallback(async () => {
    if (!order?.razorpayOrderId) {
      throw new Error('Payment order is not ready');
    }
    setPaying(true);
    setProgress(15);
    setError('');
    try {
      setProgress(40);
      const paymentId = `pay_mock_${Date.now()}`;
      let signature;

      if (isMockOrder(order.razorpayOrderId)) {
        signature = await signMockPayment(order.razorpayOrderId, paymentId);
      } else {
        throw new Error(
          'Live Razorpay Checkout is not wired in this demo. Use mock mode (default).'
        );
      }

      setProgress(70);
      const result = await paymentService.verify({
        razorpayOrderId: order.razorpayOrderId,
        razorpayPaymentId: paymentId,
        razorpaySignature: signature,
      });
      setProgress(100);
      setPayment(result);
      return result;
    } catch (err) {
      setError(err.message || 'Payment failed');
      throw err;
    } finally {
      setPaying(false);
    }
  }, [order]);

  return {
    booking,
    order,
    payment,
    loading,
    paying,
    progress,
    error,
    setError,
    pay,
  };
}
