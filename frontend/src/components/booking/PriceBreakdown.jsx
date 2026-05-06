import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import { Box, Divider, Stack, Tooltip, Typography } from '@mui/material';
import { formatCurrency } from '../../utils/format';
import { TAX_RATE } from '../../utils/priceCalculation';

/**
 * Displays room charges, per-room lines, taxes (with rate tooltip), discounts, grand total.
 */
export default function PriceBreakdown({ price, confirmedTotal }) {
  const taxPercent = Math.round(TAX_RATE * 100);

  return (
    <Stack spacing={1.25} data-testid="price-breakdown">
      {(price.lines || []).map((line) => (
        <Row
          key={line.roomId}
          label={line.label}
          value={formatCurrency(line.subtotal)}
        />
      ))}
      <Row label="Room charges" value={formatCurrency(price.roomCharges)} />
      <Row label="Nights" value={String(price.nights)} />
      {price.roomCount > 1 && <Row label="Rooms" value={String(price.roomCount)} />}
      {price.discount > 0 && (
        <Row label="Discount" value={`− ${formatCurrency(price.discount)}`} emphasize />
      )}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2, alignItems: 'center' }}>
        <Stack direction="row" spacing={0.5} alignItems="center">
          <Typography color="text.secondary">Taxes (GST est.)</Typography>
          <Tooltip
            title={`Estimated GST at ${taxPercent}% of room charges. Final tax may differ per Booking Engine rules.`}
            arrow
          >
            <InfoOutlinedIcon
              fontSize="small"
              color="action"
              data-testid="tax-rate-tooltip"
              aria-label={`Tax rate ${taxPercent} percent`}
              sx={{ cursor: 'help' }}
            />
          </Tooltip>
        </Stack>
        <Typography fontWeight={600}>{formatCurrency(price.taxes)}</Typography>
      </Box>
      <Row label="Service charges" value={formatCurrency(price.serviceCharges)} />
      <Divider sx={{ my: 1 }} />
      <Row label="Grand total (estimate)" value={formatCurrency(price.grandTotal)} bold />
      {confirmedTotal != null && (
        <Row label="Booking engine total" value={formatCurrency(confirmedTotal)} bold />
      )}
      <Typography variant="caption" color="text.secondary">
        Estimate is for display. Final total is calculated by the Booking Engine when the booking is
        created.
      </Typography>
    </Stack>
  );
}

function Row({ label, value, bold, emphasize }) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2 }}>
      <Typography color="text.secondary" fontWeight={bold ? 700 : 400}>
        {label}
      </Typography>
      <Typography
        fontWeight={bold ? 700 : 600}
        color={emphasize ? 'success.main' : 'text.primary'}
      >
        {value}
      </Typography>
    </Box>
  );
}
