import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Grid,
  MenuItem,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { defaultSearchState, toSearchParams, validateSearchState } from '../../utils/searchParams';

const ROOM_TYPES = ['', 'STANDARD', 'DELUXE', 'EXECUTIVE', 'SUITE', 'FAMILY', 'PRESIDENTIAL'];

/**
 * Shared search bar used on landing hero and rooms page.
 * Navigates to /rooms?query… so results are shareable.
 */
export function SearchBar({ initialValues, compact = false }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({ ...defaultSearchState(), ...initialValues });
  const [errors, setErrors] = useState({});

  const update = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const nextErrors = validateSearchState(form);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;
    navigate(`/rooms?${toSearchParams({ ...form, page: 0 }).toString()}`);
  };

  return (
    <Paper
      component="form"
      onSubmit={handleSubmit}
      elevation={compact ? 1 : 4}
      sx={{ p: { xs: 2, md: 3 }, borderRadius: 2 }}
    >
      {!compact && (
        <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>
          Find your room
        </Typography>
      )}
      <Grid container spacing={2} alignItems="center">
        <Grid item xs={12} sm={6} md={3}>
          <TextField
            label="Location"
            value={form.location}
            onChange={update('location')}
            fullWidth
            helperText="Single-property hotel · Mumbai"
          />
        </Grid>
        <Grid item xs={6} sm={3} md={2}>
          <TextField
            label="Check-in"
            type="date"
            value={form.checkIn}
            onChange={update('checkIn')}
            InputLabelProps={{ shrink: true }}
            error={Boolean(errors.checkIn)}
            helperText={errors.checkIn}
            fullWidth
            required
          />
        </Grid>
        <Grid item xs={6} sm={3} md={2}>
          <TextField
            label="Check-out"
            type="date"
            value={form.checkOut}
            onChange={update('checkOut')}
            InputLabelProps={{ shrink: true }}
            error={Boolean(errors.checkOut)}
            helperText={errors.checkOut}
            fullWidth
            required
          />
        </Grid>
        <Grid item xs={6} sm={4} md={1.5}>
          <TextField
            label="Guests"
            type="number"
            inputProps={{ min: 1, max: 10 }}
            value={form.guests}
            onChange={update('guests')}
            error={Boolean(errors.guests)}
            helperText={errors.guests}
            fullWidth
          />
        </Grid>
        <Grid item xs={6} sm={4} md={2}>
          <TextField select label="Room type" value={form.roomType} onChange={update('roomType')} fullWidth>
            {ROOM_TYPES.map((type) => (
              <MenuItem key={type || 'any'} value={type}>
                {type || 'Any type'}
              </MenuItem>
            ))}
          </TextField>
        </Grid>
        <Grid item xs={12} sm={4} md={1.5}>
          <Button type="submit" variant="contained" size="large" fullWidth startIcon={<SearchIcon />}>
            Search
          </Button>
        </Grid>
      </Grid>
      <Box sx={{ mt: 1 }}>
        <Typography variant="caption" color="text.secondary">
          Availability for dates is confirmed at booking time via the Booking API.
        </Typography>
      </Box>
    </Paper>
  );
}
