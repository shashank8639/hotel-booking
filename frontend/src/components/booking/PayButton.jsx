import { Button, Stack } from '@mui/material';
import { formatCurrency } from '../../utils/format';

/**
 * Pay / retry / return actions — extracted from PaymentPanel.
 */
export default function PayButton({
  amount,
  paying,
  disabled,
  showRetry,
  onPay,
  onRetry,
  onReturn,
}) {
  return (
    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} data-testid="pay-actions">
      <Button
        variant="contained"
        size="large"
        onClick={onPay}
        disabled={disabled || paying}
        data-testid="pay-now"
      >
        {paying ? 'Paying…' : `Pay ${formatCurrency(amount)}`}
      </Button>
      {showRetry && (
        <Button variant="outlined" onClick={onRetry} disabled={paying}>
          Retry payment
        </Button>
      )}
      <Button variant="text" onClick={onReturn} disabled={paying}>
        Return to booking
      </Button>
    </Stack>
  );
}
