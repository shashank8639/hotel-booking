import { Container, Paper } from '@mui/material';
import BookingStepper from '../components/booking/BookingStepper';

/**
 * Shared shell for booking wizard / payment / result pages.
 * Keeps stepper + max-width consistent across steps.
 */
export default function BookingLayout({ activeStep, children, maxWidth = 'md' }) {
  return (
    <Container maxWidth={maxWidth} sx={{ py: { xs: 2, md: 4 } }}>
      <BookingStepper activeStep={activeStep} />
      <Paper sx={{ p: { xs: 2, sm: 3 } }} elevation={0} variant="outlined">
        {children}
      </Paper>
    </Container>
  );
}
