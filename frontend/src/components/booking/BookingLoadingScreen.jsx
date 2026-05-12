import { Box, CircularProgress, LinearProgress, Stack, Typography } from '@mui/material';

/**
 * Full-step loading / payment progress screens.
 */
export default function BookingLoadingScreen({
  message = 'Loading…',
  progress,
  showLinear = false,
}) {
  return (
    <Box
      sx={{ py: 8, display: 'flex', justifyContent: 'center' }}
      data-testid="booking-loading"
      role="status"
      aria-live="polite"
    >
      <Stack spacing={2} alignItems="center" sx={{ width: '100%', maxWidth: 360 }}>
        <CircularProgress />
        <Typography>{message}</Typography>
        {showLinear && (
          <LinearProgress
            variant={typeof progress === 'number' ? 'determinate' : 'indeterminate'}
            value={progress}
            sx={{ width: '100%' }}
          />
        )}
      </Stack>
    </Box>
  );
}
