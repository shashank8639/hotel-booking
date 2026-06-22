import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Button,
  Grid,
  MenuItem,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { cityService } from '../../services/hotelService';

/**
 * Platform search bar — city + hotel name → /hotels?…
 */
export function HotelSearchBar() {
  const navigate = useNavigate();
  const [cities, setCities] = useState([]);
  const [citySlug, setCitySlug] = useState('hyderabad');
  const [name, setName] = useState('');

  useEffect(() => {
    cityService.list().then(setCities).catch(() => setCities([]));
  }, []);

  const handleSubmit = (event) => {
    event.preventDefault();
    const params = new URLSearchParams();
    if (citySlug) params.set('citySlug', citySlug);
    if (name.trim()) params.set('name', name.trim());
    navigate(`/hotels?${params.toString()}`);
  };

  return (
    <Paper component="form" onSubmit={handleSubmit} elevation={4} sx={{ p: { xs: 2, md: 3 }, borderRadius: 2 }}>
      <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>
        Search hotels
      </Typography>
      <Grid container spacing={2} alignItems="center">
        <Grid item xs={12} md={5}>
          <TextField
            select
            fullWidth
            label="City"
            value={citySlug}
            onChange={(e) => setCitySlug(e.target.value)}
          >
            {cities.map((c) => (
              <MenuItem key={c.id} value={c.slug}>
                {c.name}
              </MenuItem>
            ))}
          </TextField>
        </Grid>
        <Grid item xs={12} md={5}>
          <TextField
            fullWidth
            label="Hotel name (optional)"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </Grid>
        <Grid item xs={12} md={2}>
          <Button type="submit" fullWidth variant="contained" size="large" startIcon={<SearchIcon />}>
            Search
          </Button>
        </Grid>
      </Grid>
    </Paper>
  );
}
