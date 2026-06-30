import { Button, Paper, Stack, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

const ACTIONS = [
  { label: 'Manage rooms', to: '/admin/rooms' },
  { label: 'View bookings', to: '/admin/bookings' },
  { label: 'Payments', to: '/admin/payments' },
  { label: 'Open reports', to: '/admin/reports' },
];

export default function QuickActions() {
  return (
    <Paper variant="outlined" sx={{ p: 2 }} data-testid="quick-actions">
      <Typography fontWeight={700} gutterBottom>
        Quick actions
      </Typography>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} flexWrap="wrap" useFlexGap>
        {ACTIONS.map((a) => (
          <Button key={a.to} component={RouterLink} to={a.to} variant="outlined" size="small">
            {a.label}
          </Button>
        ))}
      </Stack>
    </Paper>
  );
}
