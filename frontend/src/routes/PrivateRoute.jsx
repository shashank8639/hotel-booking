import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import { useAuth } from '../hooks/useAuth';

/**
 * Authentication guard: only logged-in users pass.
 * Unauthenticated users are redirected to /login with return URL.
 */
export function PrivateRoute() {
  const { isAuthenticated, loading, bootstrapped } = useAuth();
  const location = useLocation();

  if (!bootstrapped || loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
        <CircularProgress aria-label="Loading session" />
      </Box>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
