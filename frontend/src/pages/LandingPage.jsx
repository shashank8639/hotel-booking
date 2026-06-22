import { useEffect, useState } from 'react';
import { Box, Button, Container, Grid, Stack, Typography } from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { HeroSection } from '../components/home/HeroSection';
import {
  CtaSection,
  DestinationsSection,
  NewsletterSection,
  ServicesSection,
  TestimonialsSection,
  SectionHeader,
} from '../components/home/MarketingSections';
import HotelCard from '../components/hotels/HotelCard';
import { ErrorState } from '../components/common/ErrorState';
import { cityService, hotelService } from '../services/hotelService';
import { usePageMeta } from '../utils/practiceStores';

/**
 * Multi-hotel platform landing — destinations + featured hotels (Module 16).
 */
export default function LandingPage() {
  usePageMeta({
    title: 'StayFinder Telangana · Hotels across the state',
    description: 'Search and book hotels in Hyderabad, Warangal, and cities across Telangana.',
  });

  const navigate = useNavigate();
  const [hotels, setHotels] = useState([]);
  const [cities, setCities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      try {
        const [featured, popular] = await Promise.all([
          hotelService.featured(),
          cityService.popular(),
        ]);
        if (!cancelled) {
          setHotels(featured || []);
          setCities(popular || []);
        }
      } catch (err) {
        if (!cancelled) setError(err.message || 'Could not load hotels');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <Box>
      <HeroSection />

      <Container maxWidth="lg" sx={{ py: { xs: 5, md: 7 } }}>
        <Stack spacing={7}>
          <Box>
            <SectionHeader
              title="Popular destinations"
              subtitle="Start with Telangana cities — architecture scales to all of India later."
            />
            <Grid container spacing={2}>
              {cities.map((city) => (
                <Grid item xs={6} sm={4} md={3} key={city.id}>
                  <Button
                    fullWidth
                    variant="outlined"
                    onClick={() => navigate(`/hotels?citySlug=${city.slug}`)}
                    sx={{ py: 2, justifyContent: 'start' }}
                  >
                    <Box textAlign="left">
                      <Typography fontWeight={700}>{city.name}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {city.stateName}
                      </Typography>
                    </Box>
                  </Button>
                </Grid>
              ))}
            </Grid>
          </Box>

          <Box>
            <SectionHeader
              title="Featured hotels"
              subtitle="Approved & verified properties from GET /api/hotels/featured."
            />
            {loading && <Typography>Loading featured hotels…</Typography>}
            {error && <ErrorState message={error} />}
            {!loading && !error && (
              <Grid container spacing={2}>
                {hotels.map((hotel) => (
                  <Grid item xs={12} sm={6} md={4} key={hotel.id}>
                    <HotelCard hotel={hotel} />
                  </Grid>
                ))}
              </Grid>
            )}
            <Box sx={{ mt: 3 }}>
              <Button component={RouterLink} to="/hotels" variant="contained" sx={{ mr: 1 }}>
                Search all hotels
              </Button>
              <Button component={RouterLink} to="/rooms" variant="outlined">
                Browse rooms (legacy)
              </Button>
            </Box>
          </Box>

          <ServicesSection />
          <DestinationsSection />
          <TestimonialsSection />
          <CtaSection />
          <NewsletterSection />
        </Stack>
      </Container>
    </Box>
  );
}
