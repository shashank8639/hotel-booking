import { Paper, Typography } from '@mui/material';
import { useAuth } from '../hooks/useAuth';

/** Customer area placeholder — full UI in Module 14. */
export function CustomerDashboardPage() {
  const { user } = useAuth();
  return (
    <Paper sx={{ p: 4 }}>
      <Typography variant="h5" gutterBottom>
        Customer Dashboard
      </Typography>
      <Typography color="text.secondary">
        Hello {user?.firstName}. My Bookings, profile, and history arrive in Module 14.
      </Typography>
    </Paper>
  );
}

export function ReceptionDashboardPage() {
  return (
    <Paper sx={{ p: 4 }}>
      <Typography variant="h5" gutterBottom>
        Reception Dashboard
      </Typography>
      <Typography color="text.secondary">
        Frontend supports RECEPTIONIST role for RBAC demos. Backend role seed is ADMIN/CUSTOMER today.
      </Typography>
    </Paper>
  );
}
