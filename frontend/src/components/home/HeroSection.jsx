import { Box, Button, Container, Stack, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { HotelSearchBar } from './HotelSearchBar';

/**
 * Full-bleed hero for the multi-hotel platform.
 */
export function HeroSection() {
  return (
    <Box
      sx={{
        position: 'relative',
        minHeight: { xs: 520, md: 620 },
        display: 'flex',
        alignItems: 'flex-end',
        color: 'common.white',
        backgroundImage:
          'linear-gradient(180deg, rgba(13,33,55,0.25) 0%, rgba(13,33,55,0.78) 70%), url(https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1800&q=80)',
        backgroundSize: 'cover',
        backgroundPosition: 'center',
      }}
    >
      <Container maxWidth="lg" sx={{ pb: { xs: 4, md: 6 }, pt: { xs: 10, md: 14 } }}>
        <Stack spacing={1.5} sx={{ maxWidth: 720, mb: 3 }}>
          <Typography
            variant="h2"
            sx={{
              fontWeight: 800,
              fontSize: { xs: '2.2rem', md: '3.4rem' },
              letterSpacing: '-0.02em',
              lineHeight: 1.1,
            }}
          >
            Find hotels across Telangana
          </Typography>
          <Typography variant="h6" sx={{ fontWeight: 400, opacity: 0.95 }}>
            Hyderabad, Warangal, Karimnagar, and more — book rooms at verified properties.
          </Typography>
          <Box>
            <Button
              component={RouterLink}
              to="/hotels"
              variant="contained"
              color="secondary"
              size="large"
              sx={{ mr: 1 }}
            >
              Explore hotels
            </Button>
          </Box>
        </Stack>
        <HotelSearchBar />
      </Container>
    </Box>
  );
}
