import { Paper, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { Button } from '@mui/material';

export default function HomePage() {
  return (
    <Paper sx={{ p: 4 }}>
      <Typography variant="h4" gutterBottom>
        Welcome to Hotel Booking
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        Module 10 — React Authentication is ready. Browse rooms or sign in to continue.
      </Typography>
      <Button variant="contained" component={RouterLink} to="/rooms" sx={{ mr: 1 }}>
        Browse rooms
      </Button>
      <Button variant="outlined" component={RouterLink} to="/login">
        Login
      </Button>
    </Paper>
  );
}
