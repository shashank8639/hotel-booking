import { Step, StepLabel, Stepper, useMediaQuery, useTheme } from '@mui/material';

export const BOOKING_STEPS = [
  'Select Room',
  'Guest Details',
  'Booking Summary',
  'Payment',
  'Confirmation',
];

/**
 * Multi-step progress indicator for the booking journey.
 * activeStep: 0–4 (Confirmation is typically shown on success page).
 */
export default function BookingStepper({ activeStep = 0 }) {
  const theme = useTheme();
  const compact = useMediaQuery(theme.breakpoints.down('sm'));

  return (
    <Stepper
      activeStep={activeStep}
      alternativeLabel={!compact}
      orientation={compact ? 'vertical' : 'horizontal'}
      sx={{ mb: 3 }}
    >
      {BOOKING_STEPS.map((label) => (
        <Step key={label}>
          <StepLabel>{label}</StepLabel>
        </Step>
      ))}
    </Stepper>
  );
}
