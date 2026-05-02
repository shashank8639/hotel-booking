import { Paper, Stack, Typography } from '@mui/material';
import { formatCurrency } from '../../utils/format';
import PriceBreakdown from './PriceBreakdown';

/**
 * Payment order lines — extracted from PaymentPanel for single responsibility.
 */
export default function OrderSummary({ booking, order, estimate }) {
  return (
    <Stack spacing={2} data-testid="order-summary">
      <Stack spacing={1.5}>
        <Row label="Booking total" value={formatCurrency(booking?.totalAmount)} />
        <Row label="Payment id" value={order?.paymentId} />
        <Row label="Razorpay order" value={order?.razorpayOrderId || '—'} />
        <Row label="Payable amount" value={formatCurrency(order?.amount)} />
        <Row label="Order status" value={order?.status} />
      </Stack>

      {estimate && (
        <Paper variant="outlined" sx={{ p: 2 }}>
          <Typography fontWeight={700} gutterBottom>
            Price breakdown
          </Typography>
          <PriceBreakdown price={estimate} confirmedTotal={booking?.totalAmount} />
        </Paper>
      )}
    </Stack>
  );
}

function Row({ label, value }) {
  return (
    <Stack direction="row" justifyContent="space-between" spacing={2}>
      <Typography color="text.secondary">{label}</Typography>
      <Typography fontWeight={600}>{String(value ?? '—')}</Typography>
    </Stack>
  );
}
