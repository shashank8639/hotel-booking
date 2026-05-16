import { Button, Paper, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { getDefaultHomePath } from '../auth/roles';

export default function UnauthorizedPage() {
  const { isAuthenticated, roles } = useAuth();
  const home = isAuthenticated ? getDefaultHomePath(roles) : '/login';

  return (
    <Paper sx={{ p: 4, textAlign: 'center' }}>
      <Typography variant="h4" gutterBottom>
        403 — Unauthorized
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        You are signed in, but your role cannot access this page.
      </Typography>
      <Button variant="contained" component={RouterLink} to={home}>
        Go to my home
      </Button>
    </Paper>
  );
}
