import { Box, Container, Divider, Grid, Link, Stack, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { PLATFORM } from '../../assets/hotelContent';

export function SiteFooter() {
  return (
    <Box component="footer" sx={{ bgcolor: '#0d2137', color: 'grey.300', mt: 6, pt: 6, pb: 3 }}>
      <Container maxWidth="lg">
        <Grid container spacing={4}>
          <Grid item xs={12} md={4}>
            <Typography variant="h6" color="common.white" gutterBottom>
              {PLATFORM.name}
            </Typography>
            <Typography variant="body2" sx={{ maxWidth: 320 }}>
              {PLATFORM.description}
            </Typography>
          </Grid>
          <Grid item xs={6} md={2}>
            <Typography variant="subtitle2" color="common.white" gutterBottom>
              Explore
            </Typography>
            <Stack spacing={1}>
              <Link component={RouterLink} to="/" color="inherit" underline="hover">
                Home
              </Link>
              <Link component={RouterLink} to="/hotels" color="inherit" underline="hover">
                Hotels
              </Link>
              <Link component={RouterLink} to="/rooms" color="inherit" underline="hover">
                Rooms
              </Link>
              <Link component={RouterLink} to="/login" color="inherit" underline="hover">
                Login
              </Link>
            </Stack>
          </Grid>
          <Grid item xs={6} md={3}>
            <Typography variant="subtitle2" color="common.white" gutterBottom>
              Contact
            </Typography>
            <Typography variant="body2">{PLATFORM.address}</Typography>
            <Typography variant="body2">{PLATFORM.phone}</Typography>
            <Typography variant="body2">{PLATFORM.email}</Typography>
          </Grid>
          <Grid item xs={12} md={3}>
            <Typography variant="subtitle2" color="common.white" gutterBottom>
              Stay in the loop
            </Typography>
            <Typography variant="body2">
              Newsletter signup is a UI placeholder — wire email capture later.
            </Typography>
          </Grid>
        </Grid>
        <Divider sx={{ my: 3, borderColor: 'rgba(255,255,255,0.12)' }} />
        <Typography variant="caption" display="block" align="center">
          © {new Date().getFullYear()} {PLATFORM.name}. Multi-hotel platform · Telangana catalog.
        </Typography>
      </Container>
    </Box>
  );
}
