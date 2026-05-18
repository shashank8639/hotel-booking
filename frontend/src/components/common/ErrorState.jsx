import { Alert, Box, Button, Typography } from '@mui/material';
import HotelOutlinedIcon from '@mui/icons-material/HotelOutlined';
import SearchOffOutlinedIcon from '@mui/icons-material/SearchOffOutlined';

export function ErrorState({ message, onRetry }) {
  return (
    <Box sx={{ py: 4 }}>
      <Alert
        severity="error"
        action={
          onRetry ? (
            <Button color="inherit" size="small" onClick={onRetry}>
              Retry
            </Button>
          ) : null
        }
      >
        {message || 'Something went wrong'}
      </Alert>
    </Box>
  );
}

/**
 * Friendly empty state with a simple illustration (icon composition — no new assets).
 */
export function EmptyState({ title, body, action }) {
  return (
    <Box
      sx={{
        py: 6,
        px: 2,
        textAlign: 'center',
        border: '1px dashed',
        borderColor: 'divider',
        borderRadius: 3,
        bgcolor: 'background.paper',
      }}
    >
      <Box
        sx={{
          width: 96,
          height: 96,
          mx: 'auto',
          mb: 2,
          borderRadius: '50%',
          display: 'grid',
          placeItems: 'center',
          bgcolor: 'primary.50',
          color: 'primary.main',
          position: 'relative',
        }}
        aria-hidden
      >
        <HotelOutlinedIcon sx={{ fontSize: 42, opacity: 0.35 }} />
        <SearchOffOutlinedIcon sx={{ fontSize: 28, position: 'absolute', right: 14, bottom: 14 }} />
      </Box>
      <Typography variant="h6" gutterBottom>
        {title || 'No results'}
      </Typography>
      <Typography color="text.secondary" sx={{ maxWidth: 420, mx: 'auto', mb: action ? 2 : 0 }}>
        {body}
      </Typography>
      {action}
    </Box>
  );
}
