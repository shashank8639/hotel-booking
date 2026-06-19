import { NavLink as RouterLink, useNavigate } from 'react-router-dom';
import { AppBar, Button, Toolbar, Typography, Stack } from '@mui/material';
import { useAuth } from '../hooks/useAuth';
import { Roles } from '../auth/roles';

/**
 * Top navigation — menus filtered by role (RBAC in the UI).
 */
export function AppNavbar() {
  const { isAuthenticated, user, roles, logout } = useAuth();
  const navigate = useNavigate();

  const isAdmin = roles.includes(Roles.ADMIN);
  const isReception = roles.includes(Roles.RECEPTIONIST);
  const isCustomer = roles.includes(Roles.CUSTOMER);

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <AppBar position="sticky" elevation={1}>
      <Toolbar sx={{ gap: 1, flexWrap: 'wrap' }}>
        <Typography
          variant="h6"
          component={RouterLink}
          to="/"
          sx={{ flexGrow: 1, color: 'inherit', textDecoration: 'none', fontWeight: 700 }}
        >
          StayFinder
        </Typography>

        <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
          <Button color="inherit" component={RouterLink} to="/">
            Home
          </Button>
          <Button color="inherit" component={RouterLink} to="/hotels">
            Hotels
          </Button>
          <Button color="inherit" component={RouterLink} to="/rooms">
            Rooms
          </Button>

          {isAuthenticated && isCustomer && (
            <Button color="inherit" component={RouterLink} to="/customer/dashboard">
              My Dashboard
            </Button>
          )}
          {isAuthenticated && isReception && (
            <Button color="inherit" component={RouterLink} to="/reception/dashboard">
              Reception
            </Button>
          )}
          {isAuthenticated && isAdmin && (
            <>
              <Button color="inherit" component={RouterLink} to="/admin/dashboard">
                Admin
              </Button>
              <Button color="inherit" component={RouterLink} to="/admin/reports">
                Reports
              </Button>
            </>
          )}

          {!isAuthenticated ? (
            <>
              <Button color="inherit" component={RouterLink} to="/login">
                Login
              </Button>
              <Button color="secondary" variant="contained" component={RouterLink} to="/register">
                Register
              </Button>
            </>
          ) : (
            <>
              <Typography variant="body2" sx={{ opacity: 0.9, px: 1 }}>
                {user?.firstName || user?.email}
              </Typography>
              <Button color="inherit" onClick={handleLogout}>
                Logout
              </Button>
            </>
          )}
        </Stack>
      </Toolbar>
    </AppBar>
  );
}
