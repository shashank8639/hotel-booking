import { useState } from 'react';
import { Link as RouterLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  AppBar,
  Box,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  useMediaQuery,
  useTheme,
  Button,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import LogoutIcon from '@mui/icons-material/Logout';
import HomeIcon from '@mui/icons-material/Home';
import { useAuth } from '../hooks/useAuth';
import { AdminUiProvider } from '../context/AdminUiContext';
import { ADMIN_NAV } from './adminNav';

const DRAWER_WIDTH = 260;

/**
 * Responsive admin shell: permanent sidebar (md+) / temporary drawer (mobile) + top bar.
 */
export default function AdminLayout() {
  const theme = useTheme();
  const mobile = useMediaQuery(theme.breakpoints.down('md'));
  const [open, setOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const drawer = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }} data-testid="admin-sidebar">
      <Toolbar>
        <Typography variant="h6" fontWeight={750} noWrap>
          Admin Portal
        </Typography>
      </Toolbar>
      <Divider />
      <List sx={{ flex: 1, px: 1 }}>
        {ADMIN_NAV.map((item) => {
          const Icon = item.icon;
          const selected =
            location.pathname === item.path || location.pathname.startsWith(`${item.path}/`);
          return (
            <ListItemButton
              key={item.path}
              component={RouterLink}
              to={item.path}
              selected={selected}
              onClick={() => mobile && setOpen(false)}
            >
              <ListItemIcon>
                <Icon />
              </ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          );
        })}
      </List>
      <Divider />
      <Box sx={{ p: 2 }}>
        <Button
          fullWidth
          startIcon={<HomeIcon />}
          onClick={() => navigate('/')}
          sx={{ mb: 1 }}
        >
          Public site
        </Button>
        <Button
          fullWidth
          color="inherit"
          startIcon={<LogoutIcon />}
          onClick={async () => {
            await logout();
            navigate('/login');
          }}
        >
          Logout
        </Button>
      </Box>
    </Box>
  );

  return (
    <AdminUiProvider>
      <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'grey.100' }}>
        <AppBar
          position="fixed"
          color="inherit"
          elevation={0}
          sx={{
            borderBottom: 1,
            borderColor: 'divider',
            width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
            ml: { md: `${DRAWER_WIDTH}px` },
          }}
          data-testid="admin-topbar"
        >
          <Toolbar>
            {mobile && (
              <IconButton edge="start" onClick={() => setOpen(true)} aria-label="Open menu" sx={{ mr: 1 }}>
                <MenuIcon />
              </IconButton>
            )}
            <Typography variant="h6" sx={{ flex: 1 }} fontWeight={600}>
              StayFinder · Operations
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {user?.firstName} {user?.lastName}
            </Typography>
          </Toolbar>
        </AppBar>

        <Box component="nav" sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}>
          {mobile ? (
            <Drawer
              variant="temporary"
              open={open}
              onClose={() => setOpen(false)}
              ModalProps={{ keepMounted: true }}
              sx={{ '& .MuiDrawer-paper': { width: DRAWER_WIDTH } }}
            >
              {drawer}
            </Drawer>
          ) : (
            <Drawer
              variant="permanent"
              open
              sx={{
                '& .MuiDrawer-paper': {
                  width: DRAWER_WIDTH,
                  boxSizing: 'border-box',
                  borderRight: 1,
                  borderColor: 'divider',
                },
              }}
            >
              {drawer}
            </Drawer>
          )}
        </Box>

        <Box
          component="main"
          sx={{
            flexGrow: 1,
            p: { xs: 2, md: 3 },
            width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
            mt: 8,
          }}
        >
          <Outlet />
        </Box>
      </Box>
    </AdminUiProvider>
  );
}
