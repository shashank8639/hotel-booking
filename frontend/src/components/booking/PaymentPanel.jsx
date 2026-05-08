import { Alert, LinearProgress, Paper, Stack, Typography } from '@mui/material';
import OrderSummary from './OrderSummary';
import PayButton from './PayButton';

/**
 * Composes OrderSummary + PayButton with progress / error feedback.
 */
export default function PaymentPanel({
  booking,
  order,
  estimate,
  paying,
  progress,
  error,
  onPay,
  onRetry,
  onReturn,
}) {
  return (
    <Paper sx={{ p: { xs: 2, sm: 4 } }} elevation={1}>
      <Typography variant="h5" fontWeight={750} gutterBottom>
        Payment
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Booking #{booking?.id} · Secure checkout via Payment API
      </Typography>

      <OrderSummary booking={booking} order={order} estimate={estimate} />

      {paying && (
        <Stack spacing={1} sx={{ my: 2 }} data-testid="payment-progress">
          <Typography variant="body2">Processing payment…</Typography>
          <LinearProgress variant="determinate" value={progress} />
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ my: 2 }}>
          {error}
        </Alert>
      )}

      <Alert severity="info" sx={{ my: 2 }}>
        Demo mode signs a mock Razorpay payment locally, then calls{' '}
        <code>POST /payments/verify</code>. Production apps open Razorpay Checkout with{' '}
        <code>razorpayKeyId</code>.
      </Alert>

      <PayButton
        amount={order?.amount ?? booking?.totalAmount}
        paying={paying}
        disabled={!order}
        showRetry={Boolean(error)}
        onPay={onPay}
        onRetry={onRetry}
        onReturn={onReturn}
      />
    </Paper>
  );
}
