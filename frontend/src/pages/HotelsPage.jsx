import { useEffect, useMemo, useState } from 'react';
import { Link as RouterLink, useNavigate, useSearchParams } from 'react-router-dom';
import {
  Box,
  Button,
  Container,
  Grid,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import HotelCard from '../components/hotels/HotelCard';
import { ErrorState } from '../components/common/ErrorState';
import { cityService, hotelService } from '../services/hotelService';
import { usePageMeta } from '../utils/practiceStores';

/**
 * OTA-style hotel listing for Telangana (and scalable geo).
 */
export default function HotelsPage() {
  usePageMeta({
    title: 'Hotels in Telangana · StayFinder',
    description: 'Search hotels across Hyderabad, Warangal, and more Telangana cities.',
  });

  const [params, setParams] = useSearchParams();
  const navigate = useNavigate();
  const [cities, setCities] = useState([]);
  const [hotels, setHotels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const filters = useMemo(
    () => ({
      citySlug: params.get('citySlug') || '',
      city: params.get('city') || '',
      name: params.get('name') || '',
      category: params.get('category') || '',
      minStars: params.get('minStars') || '',
      minPrice: params.get('minPrice') || '',
      maxPrice: params.get('maxPrice') || '',
    }),
    [params]
  );

  const [form, setForm] = useState(filters);

  useEffect(() => {
    setForm(filters);
  }, [filters]);

  useEffect(() => {
    cityService.list().then(setCities).catch(() => setCities([]));
  }, []);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      setError('');
      try {
        const query = {
          page: 0,
          size: 24,
          sort: 'avgRating,desc',
        };
        Object.entries(filters).forEach(([k, v]) => {
          if (v) query[k] = v;
        });
        const page = await hotelService.search(query);
        if (!cancelled) setHotels(page.content || []);
      } catch (err) {
        if (!cancelled) setError(err.message || 'Could not search hotels');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [filters]);

  const submit = (event) => {
    event.preventDefault();
    const next = new URLSearchParams();
    Object.entries(form).forEach(([k, v]) => {
      if (v) next.set(k, v);
    });
    setParams(next);
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h4" fontWeight={800}>
            Hotels across Telangana
          </Typography>
          <Typography color="text.secondary">
            Search by city, name, stars, and price — same pattern Booking.com uses for catalog discovery.
          </Typography>
        </Box>

        <Box component="form" onSubmit={submit}>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                select
                fullWidth
                label="City"
                value={form.citySlug}
                onChange={(e) => setForm((f) => ({ ...f, citySlug: e.target.value, city: '' }))}
              >
                <MenuItem value="">All cities</MenuItem>
                {cities.map((c) => (
                  <MenuItem key={c.id} value={c.slug}>
                    {c.name}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label="Hotel name"
                value={form.name}
                onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
              />
            </Grid>
            <Grid item xs={6} sm={3} md={2}>
              <TextField
                select
                fullWidth
                label="Min stars"
                value={form.minStars}
                onChange={(e) => setForm((f) => ({ ...f, minStars: e.target.value }))}
              >
                <MenuItem value="">Any</MenuItem>
                {[3, 4, 5].map((s) => (
                  <MenuItem key={s} value={s}>
                    {s}+
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={6} sm={3} md={2}>
              <TextField
                select
                fullWidth
                label="Category"
                value={form.category}
                onChange={(e) => setForm((f) => ({ ...f, category: e.target.value }))}
              >
                <MenuItem value="">Any</MenuItem>
                {['BUDGET', 'BUSINESS', 'LUXURY', 'RESORT', 'HOTEL'].map((c) => (
                  <MenuItem key={c} value={c}>
                    {c}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={12} md={2}>
              <Button type="submit" variant="contained" fullWidth sx={{ height: '100%' }}>
                Search
              </Button>
            </Grid>
          </Grid>
        </Box>

        {loading && <Typography>Loading hotels…</Typography>}
        {error && <ErrorState message={error} />}
        {!loading && !error && hotels.length === 0 && (
          <Typography color="text.secondary">No hotels matched. Try Hyderabad or clear filters.</Typography>
        )}
        <Grid container spacing={2}>
          {hotels.map((hotel) => (
            <Grid item xs={12} sm={6} md={4} key={hotel.id}>
              <HotelCard hotel={hotel} />
            </Grid>
          ))}
        </Grid>

        <Button component={RouterLink} to="/" onClick={() => navigate('/')}>
          Back to home
        </Button>
      </Stack>
    </Container>
  );
}
