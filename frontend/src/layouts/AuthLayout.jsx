import { Box, Typography } from '@mui/material';
import { Outlet } from 'react-router-dom';
import { SiteFooter } from '../components/common/SiteFooter';
import { AppNavbar } from './AppNavbar';

/**
 * Public/app chrome: sticky nav + page content + footer.
 * Pages own their own Container / full-bleed sections (landing hero).
 */
export function MainLayout() {
  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', bgcolor: 'grey.50' }}>
      <AppNavbar />
      <Box component="main" sx={{ flex: 1 }}>
        <Outlet />
      </Box>
      <SiteFooter />
    </Box>
  );
}

/**
 * Centered card layout for login / register.
 */
export function AuthLayout() {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(160deg, #e3f2fd 0%, #f5f5f5 45%, #e0f2f1 100%)',
        p: 2,
      }}
    >
      <Box
        sx={{
          width: '100%',
          maxWidth: 440,
          p: { xs: 3, sm: 4 },
          borderRadius: 2,
          bgcolor: 'background.paper',
          boxShadow: 3,
        }}
      >
        <Typography variant="h5" fontWeight={700} gutterBottom align="center">
          StayFinder
        </Typography>
        <Outlet />
      </Box>
    </Box>
  );
}
